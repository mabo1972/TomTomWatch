/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.tomtomwatch;

import net.studioblueplanet.usb.UsbFile;
import net.studioblueplanet.usb.WatchInterface;
import net.studioblueplanet.generics.DirectExecutor;
import net.studioblueplanet.generics.ToolBox;
import net.studioblueplanet.ttbin.TtbinFileDefinition;
import net.studioblueplanet.settings.ConfigSettings;
import hirondelle.date4j.DateTime;
import java.util.ArrayList;


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;
import static org.junit.Assert.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;


/**
 *
 * @author jorgen
 */


@RunWith(PowerMockRunner.class)
@PrepareForTest(ToolBox.class)
public class CommunicationProcessTest
{
    private final WatchInterface      watchInterface;
    private final DirectExecutor      executor;
    private final TomTomWatchView     theView;
    private CommunicationProcess      theInstance;



    
    public CommunicationProcessTest()
    {
        watchInterface  = mock(WatchInterface.class);
        executor        = new DirectExecutor();
        theView         = mock(TomTomWatchView.class);
        // Set test values for the settings
        ConfigSettings.setPropertiesFile("src/test/resources/tomtomwatch.properties");
        ConfigSettings.getInstance();
    }
    
    @BeforeClass
    public static void setUpClass()
    {
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    private void returnFileData(UsbFile usbFile)
    {
        if (usbFile.fileId==0x00000123)
        {
            usbFile.length=3;
            usbFile.fileData=new byte[3];
            usbFile.fileData[0]=0;
            usbFile.fileData[1]=1;
            usbFile.fileData[2]=2;
        }
        else if (usbFile.fileId==0x00004321)
        {
            usbFile.length=4;
            usbFile.fileData=new byte[4];
            usbFile.fileData[0]=4;
            usbFile.fileData[1]=3;
            usbFile.fileData[2]=2;
            usbFile.fileData[3]=1;
        }
        else if (usbFile.fileId==0x00B80001)
        {
            usbFile.length=4;
            usbFile.fileData=new byte[4];
            usbFile.fileData[0]=4;
            usbFile.fileData[1]=3;
            usbFile.fileData[2]=2;
            usbFile.fileData[3]=1;
        }
        else if (usbFile.fileId==0x00B80002)
        {
            usbFile.length=4;
            usbFile.fileData=new byte[4];
            usbFile.fileData[0]=4;
            usbFile.fileData[1]=3;
            usbFile.fileData[2]=2;
            usbFile.fileData[3]=1;
        }
        
    }
    
    @Before
    public void setUp()
    {
        ArrayList<UsbFile>  watchFiles;
        
        watchFiles=new ArrayList<>();
        UsbFile file1=new UsbFile(0x00000123, 3, new byte[]{0, 1, 2});
        watchFiles.add(file1);
        UsbFile file2=new UsbFile(0x00004321, 3, new byte[]{3, 2, 1});
        watchFiles.add(file2);
        UsbFile file3=new UsbFile(0x00B80001, 3, null);
        watchFiles.add(file3);
        UsbFile file4=new UsbFile(0x00B80002, 3, null);
        watchFiles.add(file4);

        Mockito.reset(theView);
        Mockito.reset(watchInterface);

        when(watchInterface.openConnection()).thenReturn(false);
        when(watchInterface.getWatchTime()).thenReturn(new DateTime("2019-09-22 07:36:00"));
        when(watchInterface.getPreference("watchName")).thenReturn("Test Watch");
        when(watchInterface.readFirmwareVersion()).thenReturn("01.02.03");
        when(watchInterface.getProductId()).thenReturn(0x00E70000);
        when(watchInterface.getDeviceSerialNumber()).thenReturn("SerialNumber"); 
        
        when(watchInterface.getFileList(any())).thenReturn(watchFiles);
        
        // Read File; prepare for two subsequent calls

        when(watchInterface.readFile(any()))
        .thenAnswer
        ((InvocationOnMock invocation) ->
        {
            Object[]    args = invocation.getArguments();
            UsbFile usbFile =(UsbFile)args[0];
            returnFileData(usbFile);
            return false; 
        });

/*        
        doAnswer
        ((InvocationOnMock invocation) ->
        {
            Object[]    args = invocation.getArguments();
            UsbFile usbFile =(UsbFile)args[0];
            returnFileData(usbFile);
            return false; // void method, so return null
        })
        .when(watchInterface).readFile(any());
*/        
        when(watchInterface.writeVerifyFile(any())).thenReturn(false);
        when(watchInterface.deleteFile(any())).thenReturn(false);

        theInstance=new CommunicationProcess(this.watchInterface, this.executor);  
        // When started the process will connect
        theInstance.startProcess(theView);

    }
    
    @After
    public void tearDown()
    {        
        theInstance.requestStop();
    }

    /**
     * Just a simple sleep
     * @param millis Milliseconds to sleep
     */
    private void wait(int millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch(Exception e)
        {
        }        
    }
    
    /**
     * Test of startProcess method, of class CommunicationProcess.
     */
    @Test
    public void testStartProcess()
    {
        ArgumentCaptor<String>    stringCaptor=ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer>   intCaptor=ArgumentCaptor.forClass(Integer.class);
        System.out.println("startProcess");

        // The startProcess is called in the setup() method
        
        verify(watchInterface).getPreference("watchName");
        verify(theView).setDeviceName(stringCaptor.capture());
        assertEquals("Test Watch", stringCaptor.getValue());
        
        verify(watchInterface).readFirmwareVersion();
        verify(theView).setFirmwareVersion(stringCaptor.capture());
        assertEquals("01.02.03", stringCaptor.getValue());
        
        verify(watchInterface).getProductId();
        verify(theView).setProductId(intCaptor.capture());
        assertEquals(0x00E70000, (int)intCaptor.getValue());
        
        verify(watchInterface).getDeviceSerialNumber();
        verify(theView).setSerial(stringCaptor.capture());
        assertEquals("SerialNumber", stringCaptor.getValue());

        // The startProcess() method makes the process request the watch time every second
        // It immediately starts with a call, so in 2.5 sec 3 calls are to be expected
        wait(2500);
        verify(watchInterface, times(3)).getWatchTime();        
        verify(watchInterface).openConnection();
    }

    /**
     * Test of requestStop method, of class CommunicationProcess.
     */
    @Test
    public void testRequestStop()
    {
        System.out.println("requestStop");

        wait(500);
        theInstance.requestStop();
              
        // The watch timer will timeout just once after stopping the timer
        wait(1500);
        verify(watchInterface, times(1)).getWatchTime();
    }

    /**
     * Test of pushCommand method, of class CommunicationProcess.
     */
    @Test
    public void testPushCommandGetDeviceSerial()
    {
        ArgumentCaptor<DateTime>  datetimeCaptor=ArgumentCaptor.forClass(DateTime.class);
        ArgumentCaptor<String>    stringCaptor=ArgumentCaptor.forClass(String.class);

        System.out.println("pushCommand - THREADCOMMAND_GETDEVICESERIAL");
        ThreadCommand command = ThreadCommand.THREADCOMMAND_GETDEVICESERIAL;
        theInstance.pushCommand(command);
        // Once called during startup hence 2 times expected
        verify(watchInterface, times(2)).getDeviceSerialNumber();
        verify(theView, times(2)).setSerial(stringCaptor.capture());
        assertEquals("SerialNumber", stringCaptor.getValue());
    }
    
    /**
     * Test of pushCommand method, of class CommunicationProcess.
     */
    @Test
    public void testPushCommandGetTime()
    {
        ArgumentCaptor<DateTime>  datetimeCaptor=ArgumentCaptor.forClass(DateTime.class);

        System.out.println("pushCommand - THREADCOMMAND_GETTIME");
        ThreadCommand command = ThreadCommand.THREADCOMMAND_GETTIME;

        // TO DO: When started, the first call is made
        wait(500);
        verify(watchInterface, times(1)).getWatchTime();
        verify(theView, times(1)).showTime(datetimeCaptor.capture());
        
        // Second call
        theInstance.pushCommand(command);
        verify(watchInterface, times(2)).getWatchTime();
        verify(theView, times(2)).showTime(datetimeCaptor.capture());
        assertEquals("07:36:00", datetimeCaptor.getValue().format("hh:mm:ss"));
    }
    
    /**
     * Test of pushCommand method, of class CommunicationProcess.
     */
    @Test
    public void testPushCommandListRouteFiles()
    {
        ArgumentCaptor<UsbFile> fileCaptor;
        ArgumentCaptor<String>  stringCaptor;

        ArrayList<UsbFile>  watchFiles=new ArrayList<>();
        UsbFile file1=new UsbFile(0x00B80001, 4, null);
        watchFiles.add(file1);
        UsbFile file2=new UsbFile(0x00B80002, 4, null);
        watchFiles.add(file2);
        when(watchInterface.getFileList(any())).thenReturn(watchFiles);
        
        fileCaptor  =ArgumentCaptor.forClass(UsbFile.class);
        stringCaptor=ArgumentCaptor.forClass(String.class);
        
        System.out.println("pushCommand - THREADCOMMAND_DOWNLOADROUTES");
        ThreadCommand command = ThreadCommand.THREADCOMMAND_DOWNLOADROUTES;

        // Good flow
        theInstance.pushCommand(command);
        verify(watchInterface, times(2)).readFile(fileCaptor.capture());
        assertEquals(0x00b80001, fileCaptor.getAllValues().get(0).fileId);
        assertEquals(0x00b80002, fileCaptor.getAllValues().get(1).fileId);
        assertNotEquals(null, fileCaptor.getAllValues().get(0).fileData);
        assertNotEquals(null, fileCaptor.getAllValues().get(1).fileData);
        verify(theView).addRoutesToListBoxLater(any(), anyInt());
        
        // File read error
        doReturn(true).when(watchInterface).readFile(any());
        theInstance.pushCommand(command);
        verify(theView, times(1)).showErrorDialog(stringCaptor.capture());
        assertEquals("Error reading file 0x00b80001", stringCaptor.getValue());

        // No file info
        when(watchInterface.getFileList(any())).thenReturn(null);
        theInstance.pushCommand(command);
        verify(theView, times(2)).showErrorDialog(stringCaptor.capture());
        assertEquals("Error retrieving file info from watch", stringCaptor.getValue());
    }
    

    /**
     * Test of requestSetNewDeviceName method, of class CommunicationProcess.
     */
    @Test
    public void testRequestSetNewDeviceName()
    {
        ArgumentCaptor<String>    prefCaptor=ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String>    stringCaptor=ArgumentCaptor.forClass(String.class);
        String                    name;
        
        // Bad device name
        System.out.println("requestSetNewDeviceName");
        name = "New_Device_Name#$%";
        theInstance.requestSetNewDeviceName(name);
        verify(watchInterface, never()).setPreference(any(), any());
        verify(theView).showErrorDialog(stringCaptor.capture());
        assertEquals("Illegel Watch Name "+name, stringCaptor.getValue());

        // Good flow
        name = "New_Device_Name";
        theInstance.requestSetNewDeviceName(name);
        verify(watchInterface, times(1)).setPreference(prefCaptor.capture(), stringCaptor.capture());
        assertEquals("watchName", prefCaptor.getValue());
        assertEquals(name, stringCaptor.getValue());
    }

    /**
     * Test of requestLoadActivityFromTtbinFile method, of class CommunicationProcess.
     */
    @Test
    public void testRequestLoadActivityFromTtbinFile()
    {
        ArgumentCaptor<ActivityData>    dataCaptor  =ArgumentCaptor.forClass(ActivityData.class);
        ArgumentCaptor<String>          stringCaptor=ArgumentCaptor.forClass(String.class);
        String                          fileName;
        System.out.println("requestLoadActivityFromTtbinFile");
        
        // Non existing file
        fileName="PietjePuk";
        theInstance.requestLoadActivityFromTtbinFile(fileName);
        verify(theView).showErrorDialog(stringCaptor.capture());
        verify(theView, never()).addListItem(any(), any());
        assertEquals("Error loading file: ", stringCaptor.getValue().substring(0, 20));

        // Good flow
        fileName = "src/test/resources/test.ttbin";
        theInstance.requestLoadActivityFromTtbinFile(fileName);
        verify(theView).addListItem(dataCaptor.capture(), stringCaptor.capture());
        assertEquals("file  ", stringCaptor.getValue());
        assertEquals(TtbinFileDefinition.ACTIVITY_FREESTYLE, dataCaptor.getValue().activity.getActivityType());
        assertEquals("2019-09-20", dataCaptor.getValue().activity.getStartDateTime().format("YYYY-MM-DD"));
        assertEquals(3.80455, dataCaptor.getValue().activity.getDistance(), 0.0001);
    }

    /**
     * Test of requestWriteDeviceFileToDisk method, of class CommunicationProcess.
     */
    @Test
    public void testRequestWriteDeviceFileToDisk()
    {
        ArgumentCaptor<String>  stringCaptor;
        ArgumentCaptor<byte[]>  bytesCaptor;
        byte[]                  bytes;
        int                     fileId;

        // Mock the ToolBox
        PowerMockito.mockStatic(ToolBox.class);
        
        System.out.println("requestWriteDeviceFileToDisk");
        stringCaptor=ArgumentCaptor.forClass(String.class);
        bytesCaptor =ArgumentCaptor.forClass(byte[].class);

        // Happy flow
        fileId = 0x00000123;
        when(ToolBox.writeBytesToFile(stringCaptor.capture(), bytesCaptor.capture())).thenReturn(false);
        theInstance.requestWriteDeviceFileToDisk(fileId);
        assertEquals(".\\working\\files\\0x00000123.bin", stringCaptor.getValue());
        bytes=bytesCaptor.getValue();
        assertEquals(3, bytes.length);
        assertEquals(0x00, bytes[0]);
        assertEquals(0x01, bytes[1]);
        assertEquals(0x02, bytes[2]);

        // Error writing file
        fileId = 0x00000123;
        when(ToolBox.writeBytesToFile(stringCaptor.capture(), bytesCaptor.capture())).thenReturn(true);
        theInstance.requestWriteDeviceFileToDisk(fileId);
        verify(theView, times(1)).showErrorDialog(stringCaptor.capture());
        assertEquals("Error writing file .\\working\\files\\0x00000123.bin", stringCaptor.getValue());

        // Error fetching file from watch
        fileId = 0x00000123;
        // we have to use doReturn.when here iso. when.thenReturn, otherwise null pointer exception...
        doReturn(true).when(watchInterface).readFile(any());
        theInstance.requestWriteDeviceFileToDisk(fileId);
        verify(theView, times(2)).showErrorDialog(stringCaptor.capture());
        assertEquals("Error reading file with ID 0x00000123", stringCaptor.getValue());

        // Non existing file on watch
        fileId = 0x00000001;
        theInstance.requestWriteDeviceFileToDisk(fileId);
        verify(theView, times(3)).showErrorDialog(stringCaptor.capture());
        assertEquals("File with ID 0x00000001 does not exist on watch", stringCaptor.getValue());

        // Error fetching file list
        fileId = 0x00000123;
        when(ToolBox.writeBytesToFile(stringCaptor.capture(), bytesCaptor.capture())).thenReturn(false);
        when(watchInterface.getFileList(any())).thenReturn(null);
        theInstance.requestWriteDeviceFileToDisk(fileId);
        verify(theView, times(4)).showErrorDialog(stringCaptor.capture());
        assertEquals("Error: file list could not be retrieved from watch", stringCaptor.getValue());
    }

    /**
     * Test of requestUploadFile method, of class CommunicationProcess.
     */
    @Test
    public void testRequestUploadFile()
    {
        String                      fileName;
        ArgumentCaptor<UsbFile>     fileCaptor;
        ArgumentCaptor<String>      stringCaptor;
        UsbFile                     file;
        
        System.out.println("requestUploadFile");

        fileCaptor  =ArgumentCaptor.forClass(UsbFile.class);
        stringCaptor=ArgumentCaptor.forClass(String.class);
        
        // Good flow
        fileName = "src/test/resources/0x00000123.bin";
        PowerMockito.mockStatic(ToolBox.class);
        when(ToolBox.readBytesFromFile(stringCaptor.capture())).thenReturn(new byte[]{'a', 'b', 'c', 'd'});
        theInstance.requestUploadFile(fileName);
        verify(watchInterface).writeVerifyFile(fileCaptor.capture());
        file=fileCaptor.getValue();
        assertEquals(0x00000123, file.fileId);
        assertEquals(4, file.length);
        assertEquals(fileName, stringCaptor.getValue());
        
        // Illegal file name
        fileCaptor=ArgumentCaptor.forClass(UsbFile.class);
        fileName = "src/test/resources/PietjePuk.bin";
        theInstance.requestUploadFile(fileName);
        verify(theView, times(1)).showErrorDialog(stringCaptor.capture());
        assertEquals("The filename 'PietjePuk.bin' does not fit the required format: 0xnnnnnnnn.bin", stringCaptor.getValue());
        
        // Writing to watch fails
        fileName = "src/test/resources/0x00000123.bin";
        when(watchInterface.writeVerifyFile(any())).thenReturn(true);
        theInstance.requestUploadFile(fileName);
        verify(theView, times(2)).appendStatus(stringCaptor.capture());
        assertEquals("Failed!!\n", stringCaptor.getValue());

        // Reading file fails
        fileName = "src/test/resources/0x00000123.bin";
        when(watchInterface.writeVerifyFile(any())).thenReturn(false);
        when(ToolBox.readBytesFromFile(stringCaptor.capture())).thenReturn(null);
        theInstance.requestUploadFile(fileName);
        verify(theView, times(2)).showErrorDialog(stringCaptor.capture());
        assertEquals("The filename '0x00000123.bin' could not be read", stringCaptor.getValue());

}

    /**
     * Test of requestDeleteDeviceFileFromWatch method, of class CommunicationProcess.
     */
    @Test
    public void testRequestDeleteDeviceFileFromWatch()
    {
        int fileId;
        ArgumentCaptor<UsbFile> fileCaptor;
        ArgumentCaptor<String>  stringCaptor;
        
        System.out.println("requestDeleteDeviceFileFromWatch");

        fileCaptor=ArgumentCaptor.forClass(UsbFile.class);
        stringCaptor=ArgumentCaptor.forClass(String.class);
        // Good flow
        fileId = 0x00000123;
        theInstance.requestDeleteDeviceFileFromWatch(fileId);
        verify(watchInterface).deleteFile(fileCaptor.capture());
        verify(theView).appendStatus(stringCaptor.capture());
        assertEquals(fileId, fileCaptor.getValue().fileId);
        assertEquals("File 0x00000123 deleted!", stringCaptor.getValue());
        
        // Non existing file
        fileId = 0x00000001;
        theInstance.requestDeleteDeviceFileFromWatch(fileId);
        verify(theView, atLeast(1)).showErrorDialog(stringCaptor.capture());
        assertEquals("File with ID 0x00000001 does not exist on watch", stringCaptor.getValue());
        
        // Error deleting file
        fileId = 0x00000123;
        when(watchInterface.deleteFile(any())).thenReturn(true);
        theInstance.requestDeleteDeviceFileFromWatch(fileId);
        verify(theView, atLeast(1)).showErrorDialog(stringCaptor.capture());
        assertEquals("File with ID 0x00000123 could not be deleted", stringCaptor.getValue());
        
        // Error reading file info
        fileId = 0x12300123;
        when(watchInterface.getFileList(any())).thenReturn(null);
        theInstance.requestDeleteDeviceFileFromWatch(fileId);
        verify(theView, atLeast(1)).showErrorDialog(stringCaptor.capture());
        assertEquals("Error retrieving file info from watch on file while deleting 0x12300123", stringCaptor.getValue());
    }

    /**
     * Test of requestUploadGpxFile method, of class CommunicationProcess.
     */
    @Test
    @Ignore
    public void testRequestUploadGpxFile()
    {
        System.out.println("requestUploadGpxFile");
        String file = "fileName";
        String name = "";

        PowerMockito.mockStatic(ToolBox.class);
        when(ToolBox.readStringFromUrl("fileName")).thenReturn("<gpx></wpt lat=\"52.22182\" lon=\"6.89512\"></wpt lat=\"52.22109\" lon=\"6.89304\"></gpx>");

        theInstance.requestUploadGpxFile(file, name);

    }

    /**
     * Test of requestShowFile method, of class CommunicationProcess.
     */
    @Test
    @Ignore
    public void testRequestShowFile()
    {
        System.out.println("requestShowFile");
        int fileId = 0;
        CommunicationProcess instance = null;
        instance.requestShowFile(fileId);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getActivityData method, of class CommunicationProcess.
     */
    @Test
    @Ignore
    public void testGetActivityData()
    {
        System.out.println("getActivityData");
        int index = 0;
        CommunicationProcess instance = null;
        ActivityData expResult = null;
        ActivityData result = instance.getActivityData(index);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isConnected method, of class CommunicationProcess.
     */
    @Test
    public void testIsConnected()
    {
        boolean expResult;
        boolean result;

        System.out.println("isConnected");

        // When started, the process is connected
        expResult = true;
        result = theInstance.isConnected();
        assertEquals(expResult, result);
        verify(watchInterface).openConnection();

        CommunicationProcess instance = new CommunicationProcess(this.watchInterface, this.executor);
        Mockito.reset(watchInterface);
        // When created, the process is not connected
        expResult = false;
        result = instance.isConnected();
        assertEquals(expResult, result);
        verify(watchInterface, never()).openConnection();
    }

    /**
     * Test of getDeviceName method, of class CommunicationProcess.
     */
    @Test
    public void testGetDeviceName()
    {
        System.out.println("getDeviceName");

        String expResult = "Test Watch";
        String result = theInstance.getDeviceName();
        assertEquals(expResult, result);
    }

    /**
     * Test of setTrackSmoothing method, of class CommunicationProcess.
     */
    @Test
    @Ignore
    public void testSetTrackSmoothing()
    {
        System.out.println("setTrackSmoothing");
        boolean enabled = false;
        float qFactor = 0.0F;
        CommunicationProcess instance = null;
        instance.setTrackSmoothing(enabled, qFactor);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addRouteFile method, of class CommunicationProcess.
     */
    @Test
    @Ignore
    public void testAddRouteFile()
    {
        System.out.println("addRouteFile");
        String name = "";
        String file = "";
        int index = 0;
        CommunicationProcess instance = null;
        instance.addRouteFile(name, file, index);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteAllRouteFiles method, of class CommunicationProcess.
     */
    @Test
    @Ignore
    public void testDeleteAllRouteFiles()
    {
        System.out.println("deleteAllRouteFiles");
        CommunicationProcess instance = null;
        instance.deleteAllRouteFiles();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteRouteFile method, of class CommunicationProcess.
     */
    @Test
    @Ignore
    public void testDeleteRouteFile()
    {
        System.out.println("deleteRouteFile");
        int index = 0;
        CommunicationProcess instance = null;
        instance.deleteRouteFile(index);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getRouteFile method, of class CommunicationProcess.
     */
    @Test
    @Ignore
    public void testGetRouteFile()
    {
        System.out.println("getRouteFile");
        int index = 0;
        CommunicationProcess instance = null;
        UsbFile expResult = null;
        UsbFile result = instance.getRouteFile(index);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of clear method, of class CommunicationProcess.
     */
    @Test
    @Ignore
    public void testClear()
    {
        System.out.println("clear");
        CommunicationProcess instance = null;
        instance.clear();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of reportReadProgress method, of class CommunicationProcess.
     */
    @Test
    @Ignore
    public void testReportReadProgress()
    {
        System.out.println("reportReadProgress");
        int bytesRead = 0;
        CommunicationProcess instance = null;
        instance.reportReadProgress(bytesRead);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of reportWriteProgress method, of class CommunicationProcess.
     */
    @Test
    @Ignore
    public void testReportWriteProgress()
    {
        System.out.println("reportWriteProgress");
        int bytesWritten = 0;
        CommunicationProcess instance = null;
        instance.reportWriteProgress(bytesWritten);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of loadActivityFromTtbinFile method, of class CommunicationProcess.
     */
    @Test
    @Ignore
    public void testLoadActivityFromTtbinFile()
    {
        System.out.println("loadActivityFromTtbinFile");
        CommunicationProcess instance = null;
        boolean expResult = false;
        boolean result = instance.loadActivityFromTtbinFile();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of listHistory method, of class CommunicationProcess.
     */
    @Test
    @Ignore
    public void testListHistory()
    {
        System.out.println("listHistory");
        WatchInterface watchInterface = null;
        CommunicationProcess instance = null;
        instance.listHistory(watchInterface);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of saveSimulationSet method, of class CommunicationProcess.
     */
    @Test
    @Ignore
    public void testSaveSimulationSet()
    {
        System.out.println("saveSimulationSet");
        WatchInterface watchInterface = null;
        CommunicationProcess instance = null;
        instance.saveSimulationSet(watchInterface);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
