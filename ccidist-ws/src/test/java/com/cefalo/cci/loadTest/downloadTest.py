# Send an HTTP request to the server with XML request values
 
import string
import random
from java.lang import String, System
from java.net import URLEncoder
from java.io import FileInputStream
from java.io import File
 
from net.grinder.script import Test
from net.grinder.plugin.http import HTTPRequest
from net.grinder.common import GrinderException
from net.grinder.script.Grinder import grinder
from HTTPClient import Codecs, NVPair
from threading import Condition

authenticationTest  = Test(1, "Access Token POST")
issueDetailTest     = Test(5, "CciNewsRoom_R2-Gazette_26-02-2013_City_1.zip issueDeatil page  test GET")
dloadEpubTest       = Test(6, "CciNewsRoom_R2-Gazette_26-02-2013_City_1.zip generated epub downlaod test GET")
publicUriTest       = Test(7, "CciNewsRoom_R2-Gazette_26-02-2013_City_1.zip generated epub public uri test GET")
containerUriTest    = Test(8, "CciNewsRoom_R2-Gazette_26-02-2013_City_1.zip generated epub container.xml test GET")
ODTest              = Test(9, "CciNewsRoom_R2-Gazette_26-02-2013_City_1.zip generated epub on demand request GET")
 
log = grinder.logger.info
# out = grinder.logger.TERMINAL
 
# static issue URLS
ISSUEURL   = "http://192.168.1.65:8080/webservice/polaris/addressa/issues/5"
EPUBURL    = "http://192.168.1.65:8080/webservice/polaris/addressa/issues/5.epub"
AUTHURL    = "http://192.168.1.65:8081/admin/polaris/addressa/accesstoken"
PUBLICURL  = "http://192.168.1.65:8080/webservice/polaris/addressa/issues/5/public"
CONTURL    = "http://192.168.1.65:8080/webservice/polaris/addressa/issues/5/META-INF/container.xml"

# onDemand status issue URLS
ODISSUE    = "http://192.168.1.65:8080/webservice/polaris/addressa/issues/8"
ODEPUB     = "http://192.168.1.65:8080/webservice/polaris/addressa/issues/8.epub"
totalNumberOfRuns = 0

class TestRunner:
    def __init__(self):
        self.cv = Condition() 
    def __call__(self):
        # grinder.statistics.delayReports = 1
        global totalNumberOfRuns
        self.cv.acquire()
        if totalNumberOfRuns == 0:
           totalNumberOfRuns += 1
           onDemandTest()
        self.cv.release()
        
        webServiceTest()

    # Scripts can optionally define a __del__ method. The Grinder
    # guarantees this will be called at shutdown once for each thread
    # It is useful for closing resources (e.g. database connections)
    # that were created in __init__.
    def __del__(self):
        grinder.logger.info("Thread shutting down")    

def getAuthHeader():
    encodedStr = "%s %s" % ("Basic", Codecs.base64Encode("admin:admin"))
    log("Encoded user:pass is..............%s" % encodedStr)
    authHeader = (NVPair("Authorization", encodedStr),)
    return authHeader

def webServiceTest():
    authKey = getAuthKey(AUTHURL, ISSUEURL)
    tokenAsQueryParam = (NVPair("accesstoken", authKey),)
    issueDetailRequest = HTTPRequest()
    issueDetailTest.record(issueDetailRequest)
    result = issueDetailRequest.GET(ISSUEURL, tokenAsQueryParam)
    if result.getStatusCode() != 200:
       grinder.statistics.forLastTest.success = 0 
       log("Issue Detail Error in %s.........................." % result.getOriginalURI())

    epubDownloadRequest = HTTPRequest()
    dloadEpubTest.record(epubDownloadRequest)
    result = epubDownloadRequest.GET(EPUBURL, tokenAsQueryParam)
    if result.getStatusCode() != 200:
       grinder.statistics.forLastTest.success = 0
       log("Epub Download Error in %s.........................." % result.getOriginalURI())

    publicRequest = HTTPRequest()
    publicUriTest.record(publicRequest)
    result = publicRequest.GET(PUBLICURL)
    if result.getStatusCode() != 200:
       grinder.statistics.forLastTest.success = 0
       log("Public Url Error in %s.........................." % result.getOriginalURI())

    containerRequest = HTTPRequest()
    containerUriTest.record(containerRequest)
    result = containerRequest.GET(CONTURL, tokenAsQueryParam)
    if result.getStatusCode() != 200:
       grinder.statistics.forLastTest.success = 0
       log("CONT Url Error in %s.........................." % result.getOriginalURI())

def getAuthKey(authResourceLink, issueLink):
    grinder.logger.info("Challenged, authenticating")
    formData = (NVPair("issueLink", issueLink),)
    authHeader = getAuthHeader()
    request = HTTPRequest(url="%s" % authResourceLink)
    request.setHeaders(authHeader)
    request.setFormData(formData)
    authenticationTest.record(request)
    return request.POST().getText()

def onDemandTest():
    # grinder.statistics.delayReports = 1
    authKey = getAuthKey(AUTHURL, ODISSUE)
    tokenAsQueryParam = (NVPair("accesstoken", authKey),)
    epubDownloadRequest = HTTPRequest()
    ODTest.record(epubDownloadRequest)
    result = epubDownloadRequest.GET(ODEPUB, tokenAsQueryParam)
    if result.getStatusCode() != 200:
       grinder.statistics.forLastTest.success = 0
       log("On Demand Epub Download Error in %s.........................." % result.getOriginalURI())





































































