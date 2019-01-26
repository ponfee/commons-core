/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2018, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package test.extract;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.BoundSheetRecord;
import org.apache.poi.hssf.record.ExtSSTRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RowRecord;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * 
 * @author Ponfee
 */
/**
 * This example shows how to use the event API for reading a file.
 */
public class XLSEventTest
    implements HSSFListener {
    private SSTRecord sstrec;

    /**
     * This method listens for incoming records and handles them as required.
     * @param record    The record that was found while reading.
     */
    @Override
    public void processRecord(Record record) {
        switch (record.getSid()) {
            // the BOFRecord can represent either the beginning of a sheet or the workbook
            case BOFRecord.sid:
                BOFRecord bof = (BOFRecord) record;
                if (bof.getType() == BOFRecord.TYPE_WORKBOOK) {
                    System.out.println("Encountered workbook");
                    // assigned to the class level member
                } else if (bof.getType() == BOFRecord.TYPE_WORKSHEET) {
                    System.out.println("Encountered sheet reference");
                } else {
                    System.out.println("others "+bof);
                }
                break;
            case BoundSheetRecord.sid:
                BoundSheetRecord bsr = (BoundSheetRecord) record;
                System.out.println("New sheet named: " + bsr.getSheetname());
                break;
            case RowRecord.sid:
                RowRecord rowrec = (RowRecord) record;
                System.out.println("Row found "+rowrec.getRowNumber()+", first column at " + rowrec.getFirstCol() + " last column at " + rowrec.getLastCol());
                break;
            // SSTRecords store a array of unique strings used in Excel.
            case SSTRecord.sid:
                sstrec = (SSTRecord) record;
                for (int k = 0; k < sstrec.getNumUniqueStrings(); k++) {
                    System.out.println("String table value " + k + " = " + sstrec.getString(k));
                }
                break;
            case NumberRecord.sid:
                NumberRecord numrec = (NumberRecord) record;
                System.out.println("Cell found with value " + numrec.getValue() + " at row " + numrec.getRow() + " and column " + numrec.getColumn());
                break;
            case LabelSSTRecord.sid:
                LabelSSTRecord lrec = (LabelSSTRecord) record;
                System.out.println("String cell found with value " + sstrec.getString(lrec.getSSTIndex())+ " at row " + lrec.getRow() + " and column " + lrec.getColumn());
                break;
            case ExtSSTRecord.sid:
                ExtSSTRecord extsstrec =  (ExtSSTRecord) record;
                for (int k = 0; k < extsstrec.getRecordSize(); k++) {
                }
            default:
                //System.out.println("==others"+record.getClass()+"---"+record.getSid());
                // discard;
        }
    }

    /**
     * Read an excel file and spit out what we find.
     *
     * @param args      Expect one argument that is the file to read.
     * @throws IOException  When there is an error processing the file.
     */
    public static void main(String[] args) throws IOException {
        // create a new file input stream with the input file specified
        // at the command line
        FileInputStream fin = new FileInputStream("e:/advices_export.xls");
        // create a new org.apache.poi.poifs.filesystem.Filesystem
        POIFSFileSystem poifs = new POIFSFileSystem(fin);
        // get the Workbook (excel part) stream in a InputStream
        InputStream din = poifs.createDocumentInputStream("Workbook");
        // construct out HSSFRequest object
        HSSFRequest req = new HSSFRequest();
        // lazy listen for ALL records with the listener shown above
        req.addListenerForAllRecords(new XLSEventTest());
        // create our event factory
        HSSFEventFactory factory = new HSSFEventFactory();
        // process our events based on the document input stream
        factory.processEvents(req, din);
        // once all the events are processed close our file input stream
        poifs.close();
        fin.close();
        // and our document input stream (don't want to leak these!)
        din.close();
        System.out.println("done.");
    }
}
