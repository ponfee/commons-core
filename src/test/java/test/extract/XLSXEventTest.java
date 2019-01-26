/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2018, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package test.extract;

import java.io.InputStream;
import java.util.Iterator;

import org.apache.poi.ooxml.util.SAXHelper;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;

public class XLSXEventTest {
    public void processOneSheet(String filename) throws Exception {
        OPCPackage pkg = OPCPackage.open(filename);
        XSSFReader r = new XSSFReader( pkg );
        SharedStringsTable sst = r.getSharedStringsTable();

        XMLReader parser = fetchSheetParser(sst);

        // To look up the Sheet Name / Sheet Order / rID,
        //  you need to process the core Workbook stream.
        // Normally it's of the form rId# or rSheet#
        InputStream sheet2 = r.getSheet("rId1");
        InputSource sheetSource = new InputSource(sheet2);
        parser.parse(sheetSource);
        sheet2.close();
    }

    public void processAllSheets(String filename) throws Exception {
        OPCPackage pkg = OPCPackage.open(filename);
        XSSFReader r = new XSSFReader( pkg );
        SharedStringsTable sst = r.getSharedStringsTable();

        XMLReader parser = fetchSheetParser(sst);

        Iterator<InputStream> sheets = r.getSheetsData();
        while(sheets.hasNext()) {
            System.out.println("Processing new sheet:\n");
            InputStream sheet = sheets.next();
            InputSource sheetSource = new InputSource(sheet);
            parser.parse(sheetSource);
            sheet.close();
            System.out.println("");
        }
    }

    public XMLReader fetchSheetParser(SharedStringsTable sst) throws SAXException, ParserConfigurationException {
        XMLReader parser = SAXHelper.newXMLReader();
        ContentHandler handler = new SheetHandler(sst);
        parser.setContentHandler(handler);
        return parser;
    }

    /**
     * See org.xml.sax.helpers.DefaultHandler javadocs
     */
    private static class SheetHandler extends DefaultHandler {
        private SharedStringsTable sst;
        private String lastContents;
        private boolean nextIsString;

        private SheetHandler(SharedStringsTable sst) {
            this.sst = sst;
        }

        public void startElement(String uri, String localName, String name,
                                 Attributes attributes) throws SAXException {
            // c => cell
            if(name.equals("c")) {
                // Print the cell reference
                System.out.print(attributes.getValue("r") + " - ");
                // Figure out if the value is an index in the SST
                String cellType = attributes.getValue("t");
                if(cellType != null && cellType.equals("s")) {
                    nextIsString = true;
                } else {
                    nextIsString = false;
                }
            }
            // Clear contents cache
            lastContents = "";
        }

        public void endElement(String uri, String localName, String name)
                throws SAXException {
            // Process the last contents as required.
            // Do now, as characters() may be called more than once
            if(nextIsString) {
                int idx = Integer.parseInt(lastContents);
                lastContents = sst.getItemAt(idx).getString();
                nextIsString = false;
            }

            // v => contents of a cell
            // Output after we've seen the string contents
            if(name.equals("v")) {
                System.out.println(lastContents);
            }
        }

        public void characters(char[] ch, int start, int length) {
            lastContents += new String(ch, start, length);
        }
    }

    public static void main(String[] args) throws Exception {
        XLSXEventTest example = new XLSXEventTest();
        //example.processOneSheet("d:/test/test_excel_3.xlsx");
        example.processAllSheets("d:/test/test_excel_3.xlsx");
    }
}