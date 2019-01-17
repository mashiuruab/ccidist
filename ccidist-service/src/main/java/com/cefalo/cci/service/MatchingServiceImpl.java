package com.cefalo.cci.service;

import static com.cefalo.cci.utils.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cefalo.cci.dao.DriverDao;
import com.cefalo.cci.dao.MatchingRulesDao;
import com.cefalo.cci.model.Content;
import com.cefalo.cci.model.DesignToEpubMapper;
import com.cefalo.cci.model.MatchingRules;
import com.cefalo.cci.model.Publication;
import com.cefalo.cci.service.cache.DeviceMatchCacheKey;
import com.google.common.base.Objects;
import com.google.common.base.Stopwatch;
import com.google.common.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MatchingServiceImpl implements MatchingService {
    private static final int MATCH_PRIO_DEVICE_NAME = 100;
    private static final int MATCH_PRIO_DIMENSION = 50;
    private static final int MATCH_PRIO_OTHERS = 10;

    private final Logger logger = LoggerFactory.getLogger(MatchingServiceImpl.class);

    @Inject
    private MatchingRulesDao matchingRulesDao;

    @Inject
    private DriverDao driverDao;

    // FIXME: Maybe use EHCache on this? We'll get free JGroups sync and clear all cache functionality with JavaMelody.
    @Inject
    private Cache<DeviceMatchCacheKey, List<MatchingRules>> deviceMatchCache;

    @Inject
    private ConcurrentMap<DeviceMatchCacheKey, Object> matchingAlgorithmLocks;

    @Override
    public List<MatchingRules> getRulesByPublication(String publicationId) {
        return matchingRulesDao.getRulesByPublicationId(publicationId);
    }

    @Override
    public MatchingRules getMatchingRules(long matchingId) {
        return matchingRulesDao.getMatchingRules(matchingId);
    }

    @Override
    @Transactional
    public void saveMathchingRulesAndDesignMapper(MatchingRules matchingRules, DesignToEpubMapper designToEpubMapper) {
        if (designToEpubMapper.getId() == 0) {
            driverDao.saveDesignMapper(designToEpubMapper);
        }
        matchingRules.setDesignToEpubMapper(designToEpubMapper);
        matchingRulesDao.saveMatchingRules(matchingRules);

        clearDeviceMatchCache(matchingRules.getPublication());
    }

    @Override
    @Transactional
    public void deleteMatchingRules(long ruleId) {
        MatchingRules rule = matchingRulesDao.getMatchingRules(ruleId);

        clearDeviceMatchCache(rule.getPublication());
        matchingRulesDao.deleteMatchingRules(rule);
    }

    @Override
    public List<MatchingRules> applyMatchingAlgorithm(Content content, Publication publication) {
        Stopwatch timer = new Stopwatch().start();

        DeviceMatchCacheKey cacheKey = DeviceMatchCacheKey.from(content, publication);
        List<MatchingRules> cachedResult = deviceMatchCache.getIfPresent(cacheKey);
        if (cachedResult != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Found cached match result for {}", content);
            }

            logMatchingAlgoTiming(timer);
            return cachedResult;
        }

        Object lockObject = new Object();
        Object currentLock = matchingAlgorithmLocks.putIfAbsent(cacheKey, lockObject);

        // We need to do a try/catch here to remove the lock in the finally clause.
        try {
            synchronized (currentLock != null ? currentLock : lockObject) {
                cachedResult = deviceMatchCache.getIfPresent(cacheKey);
                if (cachedResult != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Locking saved us some computation for {}", content);
                    }
                    return cachedResult;
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("Will compute matching rules for publication: {},  {}", publication.getId(), content);
                }

                List<MatchingRules> matchedRules = doMatchBasedOnDeviceOrResolution(
                        matchingRulesDao.getRulesByPublicationId(publication.getId()),
                        content);

                matchedRules = doExactMatchBasedBasedOnOtherProperties(matchedRules, content);
                matchedRules = prioritizeMatches(matchedRules, content);

                // Populate the cache with this.
                deviceMatchCache.put(cacheKey, matchedRules);
                return matchedRules;
            }
        } finally {
            matchingAlgorithmLocks.remove(cacheKey, lockObject);
            logMatchingAlgoTiming(timer);
        }

    }

    private void logMatchingAlgoTiming(Stopwatch timer) {
        timer.stop();
        if (logger.isTraceEnabled()) {
            logger.trace("Time to apply the matching algorithm: {}", timer);
        }
    }

    private List<MatchingRules> prioritizeMatches(final List<MatchingRules> matchedRules, final Content content) {
        final String deviceName = content.getDevice();
        final int width = content.getWidth();
        final int height = content.getHeight();

        Collections.sort(matchedRules, new Comparator<MatchingRules>() {
            @Override
            public int compare(MatchingRules firstRule, MatchingRules secondRule) {
                int priorityA = getPriorityForNameRes(firstRule);
                int priorityB = getPriorityForNameRes(secondRule);

                if (priorityA == priorityB) {
                    // If you are confused, don't worry. I am too.
                    // Basically, we do "order by w, h".
                    if (firstRule.getWidth() > secondRule.getWidth()) {
                        priorityA += MATCH_PRIO_DIMENSION;
                    } else if (secondRule.getWidth() > firstRule.getWidth()) {
                        priorityB += MATCH_PRIO_DIMENSION;
                    } else {
                        if (firstRule.getHeight() > secondRule.getHeight()) {
                            priorityA += MATCH_PRIO_DIMENSION;
                        } else if (secondRule.getHeight() > firstRule.getHeight()) {
                            priorityB += MATCH_PRIO_DIMENSION;
                        }
                    }
                }

                // These other attributes has much lower priority.
                priorityA += getPriorityForOtherAttributes(firstRule);
                priorityB += getPriorityForOtherAttributes(secondRule);

                return priorityB - priorityA;
            }

            // FIXME: This could be so much more intelligent.
            private int getPriorityForNameRes(MatchingRules rule) {
                // The name of the device is the most important criteria
                int priority = matches(
                        rule.getDeviceName(), deviceName) == MatchType.EXACT ? MATCH_PRIO_DEVICE_NAME : 0;

                // Resolution match comes second.
                MatchType widthMatch = matches(rule.getWidth(), width);
                MatchType heightMatch = matches(rule.getHeight(), height);
                if (widthMatch == MatchType.EXACT && heightMatch == MatchType.EXACT) {
                    priority += MATCH_PRIO_DIMENSION;
                }

                return priority;
            }

            private int getPriorityForOtherAttributes(MatchingRules rule) {
                // The other attributes have same priority
                int priority = isBlank(rule.getReaderVersion()) ? 0 : MATCH_PRIO_OTHERS;
                priority += isBlank(rule.getOs()) ? 0 : MATCH_PRIO_OTHERS;
                priority += isBlank(rule.getOsv()) ? 0 : MATCH_PRIO_OTHERS;

                return priority;
            }
        });

        return matchedRules;
    }

    private List<MatchingRules> doMatchBasedOnDeviceOrResolution(List<MatchingRules> allRules, Content content) {
        List<MatchingRules> matches = new ArrayList<>();
        for (MatchingRules rule : allRules) {
            MatchType deviceMatch = matches(rule.getDeviceName(), content.getDevice());
            MatchType widthMatch = matches(rule.getWidth(), content.getWidth());
            MatchType heightMatch = matches(rule.getHeight(), content.getHeight());

            if (deviceMatch != MatchType.NONE
                    && widthMatch != MatchType.NONE
                    && heightMatch != MatchType.NONE) {
                matches.add(rule);
            }
        }

        return matches;
    }

    private List<MatchingRules> doExactMatchBasedBasedOnOtherProperties(
            List<MatchingRules> matchingRules,
            Content content) {
        if (!isBlank(content.getOs())) {
            filterByOS(matchingRules, content.getOs());
        }

        if (!isBlank(content.getOsVersion())) {
            filterByOsVersion(matchingRules, content.getOsVersion());
        }

        if (!isBlank(content.getReader())) {
            filterByReaderVersion(matchingRules, content.getReader());
        }

        return matchingRules;
    }

    private void filterByOS(List<MatchingRules> matchingRules, String os) {
        List<MatchingRules> wrongOnes = new ArrayList<>();
        for (MatchingRules rule : matchingRules) {
            if (matches(rule.getOs(), os) == MatchType.NONE) {
                wrongOnes.add(rule);
            }
        }

        matchingRules.removeAll(wrongOnes);
    }

    private void filterByOsVersion(List<MatchingRules> matchingRules, String osVersion) {
        List<MatchingRules> wrongOnes = new ArrayList<>();
        for (MatchingRules rule : matchingRules) {
            if (matches(rule.getOsv(), osVersion) == MatchType.NONE) {
                wrongOnes.add(rule);
            }
        }
        matchingRules.removeAll(wrongOnes);
    }

    private void filterByReaderVersion(List<MatchingRules> matchingRules, String readerVersion) {
        List<MatchingRules> wrongOnes = new ArrayList<>();
        for (MatchingRules rule : matchingRules) {
            if (matches(rule.getReaderVersion(), readerVersion) == MatchType.NONE) {
                wrongOnes.add(rule);
            }
        }

        matchingRules.removeAll(wrongOnes);
    }

    private MatchType matches(String ruleValue, String deviceValue) {
        if (isBlank(ruleValue) || isBlank(deviceValue)) {
            return MatchType.WILDCARD;
        }

        if (Objects.equal(ruleValue, deviceValue)) {
            return MatchType.EXACT;
        }

        return MatchType.NONE;
    }

    private MatchType matches(int ruleValue, int deviceValue) {
        if (ruleValue == 0 || deviceValue == 0) {
            return MatchType.WILDCARD;
        }

        if (ruleValue == deviceValue) {
            return MatchType.EXACT;
        }

        if (ruleValue < deviceValue) {
            return MatchType.OK;
        }

        return MatchType.NONE;
    }

    private void clearDeviceMatchCache(Publication publication) {
        String publicationId = publication.getId();
        for (Entry<DeviceMatchCacheKey, List<MatchingRules>> entry : deviceMatchCache.asMap().entrySet()) {
            DeviceMatchCacheKey cacheKey = entry.getKey();
            if (cacheKey.getPublicationId().equals(publicationId)) {
                deviceMatchCache.invalidate(cacheKey);
                if (logger.isDebugEnabled()) {
                    logger.debug("Invalidated from rule cache: {} of publication: {}",
                            cacheKey.getContent(),
                            cacheKey.getPublicationId());
                }
            }
        }
    }

    private static enum MatchType {
        EXACT, OK, WILDCARD, NONE;
    }
}
