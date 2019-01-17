package com.cefalo.cci.restResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

public class IssueContentStreamingOutput implements StreamingOutput {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final InputStream binaryStream;
    private final long issueId;
    private final String filePath;

    public IssueContentStreamingOutput(InputStream inputStream, long issueId, String filePath) {
        this.binaryStream = inputStream;
        this.issueId = issueId;
        this.filePath = filePath;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException, WebApplicationException {
        Stopwatch timer = new Stopwatch().start();
        try (InputStream resource = binaryStream) {
            byte[] buf = new byte[128 * 1024];
            while (true) {
                int r = resource.read(buf);
                if (r == -1) {
                    break;
                }
                outputStream.write(buf, 0, r);
            }
        } finally {
            timer.stop();
            if (log.isTraceEnabled()) {
                log.trace("Time required to stream {} of Issue#{}: {}", filePath, issueId, timer);
            }
        }
    }
}
