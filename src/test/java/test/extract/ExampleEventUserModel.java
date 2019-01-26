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
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.ooxml.util.SAXHelper;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class ExampleEventUserModel {
    public void processWorkebook(String filename) throws Exception {
        try (OPCPackage pkg = OPCPackage.open(filename, PackageAccess.READ)) {
            XSSFReader r = new XSSFReader(pkg);
            SharedStringsTable sst = r.getSharedStringsTable();
            XMLReader parser = SAXHelper.newXMLReader();
            parser.setContentHandler(new SheetHandler(sst)); // custome handler
            for (Iterator<InputStream> iter = r.getSheetsData(); iter.hasNext();) {
                InputStream sheet = iter.next();
                InputSource sheetSource = new InputSource(sheet);
                parser.parse(sheetSource);
                sheet.close();
            }
        }
    }

    /** 
     * See org.xml.sax.helpers.DefaultHandler javadocs 重写 startElement characters endElements方法 
     */
    private static class SheetHandler extends DefaultHandler {
        private SharedStringsTable sst;
        private String lastContents;
        private boolean nextIsString; //是否为string格式标识
        private final LruCache<Integer, String> lruCache = new LruCache<>(60);
        /*private int sheetIndex = -1;
        private int curRow = 0;
        private int curCol = 0;
        private List<String> rowlist = new ArrayList<String>(); */

        /**
         * 缓存
         * @author Administrator
         *
         * @param <A>
         * @param <B>
         */
        private static class LruCache<A, B> extends LinkedHashMap<A, B> {
            private final int maxEntries;

            public LruCache(final int maxEntries) {
                super(maxEntries + 1, 1.0f, true);
                this.maxEntries = maxEntries;
            }

            @Override
            protected boolean removeEldestEntry(final Map.Entry<A, B> eldest) {
                return super.size() > maxEntries;
            }
        }

        private SheetHandler(SharedStringsTable sst) {
            this.sst = sst;
        }

        /**  
         * 该方法自动被调用，每读一行调用一次，在方法中写自己的业务逻辑即可 
         * @param sheetIndex 工作簿序号 
         * @param curRow 处理到第几行 
         * @param rowList 当前数据行的数据集合 
         */
        /* public void optRow(int sheetIndex, int curRow, List<String> rowList) {   
            String temp = "";   
            for(String str : rowList) {   
                temp += str + "_";   
            } 
            this.rowlist.clear();
            this.curRow++;
            this.curCol=0;
            System.out.println(temp);   
        } */

        @Override
        public void startElement(String uri, String localName, String name,
            Attributes attributes) throws SAXException {
            // c => cell 代表单元格
            if (name.equals("c")) {
                // Print the cell reference
                //获取单元格的位置，如A1,B1
                System.out.print(attributes.getValue("r") + " - ");
                // Figure out if the value is an index in the SST 如果下一个元素是 SST 的索引，则将nextIsString标记为true
                //单元格类型
                String cellType = attributes.getValue("t");
                //cellType值 s:字符串 b:布尔 e:错误处理
                if (cellType != null && cellType.equals("s")) {
                    //标识为true 交给后续endElement处理
                    nextIsString = true;
                } else {
                    nextIsString = false;
                }
            }
            // Clear contents cache
            lastContents = "";
        }

        /**
         * 得到单元格对应的索引值或是内容值
         * 如果单元格类型是字符串、INLINESTR、数字、日期，lastIndex则是索引值
         * 如果单元格类型是布尔值、错误、公式，lastIndex则是内容值
         */
        @Override
        public void characters(char[] ch, int start, int length)
            throws SAXException {
            lastContents += new String(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String name)
            throws SAXException {
            // Process the last contents as required.
            // Do now, as characters() may be called more than once
            if (nextIsString) {
                int idx = Integer.parseInt(lastContents);
                lastContents = lruCache.get(idx);
                //如果内容为空 或者Cache中存在相同key 不保存到Cache中
                if (lastContents == null && !lruCache.containsKey(idx)) {
                    lastContents = new XSSFRichTextString(sst.getEntryAt(idx)).toString();
                    lruCache.put(idx, lastContents);
                }
                nextIsString = false;
            }

            // v => contents of a cell
            // Output after we've seen the string contents
            if (name.equals("v")) {
                System.out.println(lastContents);
                //rowlist.add(curCol++,lastContents);
            } else {
                //如果标签名称为 row , 已到行尾
                if (name.equals("row")) {
                    //optRow(sheetIndex, curRow, rowlist);
                    System.out.println(lruCache);
                    lruCache.clear();
                }
            }
        }

    }

    public static void main(String[] args) throws Exception {
        new ExampleEventUserModel().processWorkebook("d:/abc1.xlsx");
    }

}
