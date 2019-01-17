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

uploadChronicleTest = Test(2, "Chronicle2_ipad.zip upload POST")
uploadBigTest       = Test(3, "CciNewsRoom_R2-Gazette_26-02-2013_City_1.zip upload POST")
updateChronicleTest = Test(4, "Chronicle2_update_ipad.zip upload POST")
 
log = grinder.logger.info
# out = grinder.logger.TERMINAL
 
# static issue URLS
UPLOADURL  = "http://192.168.1.65:8081/admin/edit/polaris/addressa/issues/cciobjectxml/"
UPLOADFILE1 = "C:/mygrinder/Chronicle2_ipad.zip"
UPDATEFILE1 = "C:/mygrinder/Chronicle2_update_ipad.zip"
UPLOADFILE2 = "C:/mygrinder/CciNewsRoom_R2-Gazette_26-02-2013_City_1.zip"

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
           uploadUpdate()
        self.cv.release()

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

def uploadUpdate():
    # grinder.statistics.delayReports = 1
    authHeader = getAuthHeader()
    file = File(UPLOADFILE1)
    fileStream = FileInputStream(file)
    uploadChronicleRequest = HTTPRequest()
    uploadChronicleTest.record(uploadChronicleRequest)
    uploadChronicleRequest.setHeaders(authHeader)
    result = uploadChronicleRequest.POST(UPLOADURL, fileStream)
    if result.getStatusCode() != 201:
       grinder.statistics.forLastTest.success = 0
       debugString = "%s%s" % (result.getOriginalURI(), result.getText())
       log("Uploading Chronicle2_ipad.zip Error in %s.........................." % debugString)
    fileStream.close()

    file = File(UPDATEFILE1)
    fileStream = FileInputStream(file)
    updateChronicleRequest = HTTPRequest()
    updateChronicleTest.record(updateChronicleRequest)
    updateChronicleRequest.setHeaders(authHeader)
    result = updateChronicleRequest.POST(UPLOADURL, fileStream)
    if result.getStatusCode() != 200:
       grinder.statistics.forLastTest.success = 0
       debugString = "%s%s" % (result.getOriginalURI(), result.getText())
       log("Updating Chronicle2_update_ipad.zip Error in %s.........................." % debugString)
    fileStream.close()

    file = File(UPLOADFILE2)
    fileStream = FileInputStream(file)
    uploadBigRequest = HTTPRequest()
    uploadBigTest.record(uploadBigRequest)
    uploadBigRequest.setHeaders(authHeader)
    result = uploadBigRequest.POST(UPLOADURL, fileStream)
    if result.getStatusCode() != 201:
       grinder.statistics.forLastTest.success = 0
       debugString = "%s%s" % (result.getOriginalURI(), result.getText())
       log("Uploading CciNewsRoom_R2-Gazette_26-02-2013_City_1.zip Error in %s.........................." % debugString)
    fileStream.close()







































































