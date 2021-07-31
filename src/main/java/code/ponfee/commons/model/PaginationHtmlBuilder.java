/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2018, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package code.ponfee.commons.model;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import code.ponfee.commons.http.HttpParams;

/**
 * Pagination html builder
 * 
 * @author Ponfee
 */
public final class PaginationHtmlBuilder {

    public static final String CDN_JQUERY = "<script src=\"http://libs.baidu.com/jquery/2.1.4/jquery.min.js\"></script>";
    public static final String CDN_BASE64 = "<script src=\"https://cdn.bootcss.com/Base64/1.1.0/base64.min.js\"></script>";

    private final String title;
    private final String url;
    private final int pageNum;
    private final int pageSize;
    private final long totalRecords;
    private final int totalPages;

    private String scripts = EMPTY;
    private String form    = EMPTY;
    private String table   = EMPTY;
    private String params  = EMPTY;
    private String foot    = EMPTY;

    private PaginationHtmlBuilder(String title, String url, int pageNum, 
                                  int pageSize, long totalRecords, int totalPages) {
        this.title = Optional.ofNullable(title).orElse(EMPTY);
        this.url = Optional.ofNullable(url).orElse(EMPTY);
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.totalRecords = totalRecords;
        this.totalPages = totalPages;
    }

    public static PaginationHtmlBuilder newBuilder(String title, String url, int pageNum, 
                                                   int pageSize, long totalRecords, int totalPages) {
        return new PaginationHtmlBuilder(title, url, pageNum, pageSize, totalRecords, totalPages);
    }

    public static PaginationHtmlBuilder newBuilder(String title, String url, Page<?> page) {
        return new PaginationHtmlBuilder(title, url, page.getPageNum(), 
                                         page.getPageSize(), page.getTotal(), page.getPages());
    }

    public PaginationHtmlBuilder scripts(String scripts) {
        this.scripts = Optional.ofNullable(scripts).orElse(EMPTY);
        return this;
    }

    public PaginationHtmlBuilder form(String form) {
        this.form = Optional.ofNullable(form).orElse(EMPTY);
        return this;
    }

    public PaginationHtmlBuilder table(String table) {
        this.table = Optional.ofNullable(table).orElse(EMPTY);
        return this;
    }

    public PaginationHtmlBuilder params(String params) {
        this.params = Optional.ofNullable(params).orElse(EMPTY);
        return this;
    }

    public PaginationHtmlBuilder params(Map<String, Object> params) {
        params = new HashMap<>(params);
        params.remove(PageHandler.DEFAULT_PAGE_NUM);
        params.remove(PageHandler.DEFAULT_PAGE_SIZE);
        return this.params(HttpParams.buildParams(params));
    }

    public PaginationHtmlBuilder params(PageParameter pageParams) {
        return this.params(pageParams.params());
    }

    public PaginationHtmlBuilder foot(String foot) {
        this.foot = Optional.ofNullable(foot).orElse(EMPTY);
        return this;
    }

    public String build() {
        return MessageFormat.format(
            PAGINATION_HTML, 
            title,
            scripts,
            url,
            form,
            table,
            buildPageArrow(url, pageNum - 1, pageSize, totalPages, params),
            buildInputBox(PageHandler.DEFAULT_PAGE_NUM, pageNum),
            buildPageArrow(url, pageNum + 1, pageSize, totalPages, params),
            totalRecords,
            totalPages,
            buildInputBox(PageHandler.DEFAULT_PAGE_SIZE, pageSize),
            foot
        );
    }

    // -------------------------------------------------------------------------------
    private static final String PAGINATION_HTML = new StringBuilder(8192)
        .append("<!DOCTYPE html>                                                                   \n")
        .append("<html>                                                                            \n")
        .append("  <head lang=\"en\">                                                              \n")
        .append("    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />     \n")
        .append("    <title>{0}</title>                                                            \n")
        .append("    '<style>                                                                      \n")
        .append("      * {font-family: Microsoft YaHei;}                                           \n")
        .append("      .grid {overflow-x: auto;background-color: #fff;color: #555;}                \n")
        .append("      .grid table {                                                               \n")
        .append("        width:100%;font-size:12px;border-collapse:collapse;border-style:hidden;   \n")
        .append("      }                                                                           \n")
        .append("      .grid table, div.grid table caption, div.grid table tr {                    \n")
        .append("        border: 1px solid #6d6d6d;                                                \n")
        .append("      }                                                                           \n")
        .append("      .grid table tr td, div.grid table tr th {border: 1px solid #6d6d6d;}        \n")
        .append("      .grid table caption {                                                       \n")
        .append("        font-size:14px; padding:5px;                                              \n")
        .append("        background:#e6e6fa; font-weight:bolder; border-bottom:none;               \n")
        .append("      }                                                                           \n")
        .append("      .grid table thead th {padding: 5px;background: #ccc;}                       \n")
        .append("      .grid table td {text-align: center;padding: 3px;}                           \n")
        .append("      .grid table td.text-left, .grid table th.text-left {text-align:left;}       \n")
        .append("      .grid table td.text-right, .grid table th.text-right {text-align:right;}    \n")
        .append("      .grid table td.text-center, .grid table th.text-center {text-align:center;} \n")
        .append("      .grid table tfoot th {padding: 5px;}                                        \n")
        .append("      .grid table tr:nth-child(odd) td{background:#fff;}                          \n")
        .append("      .grid table tr:nth-child(even) td{background: #e8e8e8}                      \n")
        .append("      .grid p.remark {font-size: 14px;}                                           \n")
        .append("      .grid .nowrap {                                                             \n")
        .append("        white-space:nowrap; word-break:keep-all;                                  \n")
        .append("        overflow:hidden; text-overflow:ellipsis; max-width:200px;                 \n")
        .append("      }                                                                           \n")
        .append("                                                                                  \n")
        .append("      .container {                                                                \n")
        .append("        background:#fdfdfd; padding:1rem; margin:3rem auto;                       \n")
        .append("        border-radius:0.2rem; counter-reset:page; text-align:center;              \n")
        .append("      }                                                                           \n")
        .append("      .container:after {clear:both; content:\"\"; display:table;}                 \n")
        .append("      .container ul {width:100%;width:45rem;}                                     \n")
        .append("      .page ul, li {list-style:none; display:inline; padding-left:0px;}           \n")
        .append("      .page li {counter-increment:page;}                                          \n")
        .append("      .page li:hover a, .page li.active a {                                       \n")
        .append("        color:#fdfdfd; background-color:#1d1f20; border:solid 1px #1d1f20;        \n")
        .append("      }                                                                           \n")
        .append("      .page li:first-child a:after {content:\"<\";}                               \n")
        .append("      .page li:nth-child(2) {counter-reset:page;}                                 \n")
        .append("      .page li:last-child a:after {content:\"\";}                                 \n")
        .append("      .page li a {                                                                \n")
        .append("        border:solid 1px #d6d6d6; border-radius:0.2rem; color:#7d7d7d;            \n")
        .append("        text-decoration:none; text-transform:uppercase; display:inline-block;     \n")
        .append("        text-align:center; padding:0.5rem 0.9rem;                                 \n")
        .append("      }                                                                           \n")
        .append("      .page li              a       {display:none;}                               \n")
        .append("      .page li:first-child  a       {display:inline-block;}                       \n")
        .append("      .page li:first-child  a:after {content:\"<\";}                              \n")
        .append("      .page li:nth-child(2) a       {display:inline-block;}                       \n")
        .append("      .page li:nth-child(3) a       {display:inline-block;}                       \n")
        .append("      .page li:nth-child(4) a       {display:inline-block;}                       \n")
        .append("      .page li:nth-child(5) a       {display:inline-block;}                       \n")
        .append("      .page li:nth-child(6) a       {display:inline-block;}                       \n")
        .append("      .page li:nth-child(7) a       {display:inline-block;}                       \n")
        .append("      .page li:nth-child(8) a       {display:inline-block;}                       \n")
        .append("      .page li:last-child   a       {display:inline-block;}                       \n")
        .append("      .page li:last-child   a:after {content:\">\";}                              \n")
        .append("      .page li:nth-last-child(2) a  {display:inline-block;}                       \n")
        .append("      .page li:nth-last-child(3)    {display:inline-block;}                       \n")
        .append("    </style>'                                                                     \n")
        .append("    {1}                                                                           \n")
        .append("  </head>                                                                         \n")
        .append("  <body>                                                                          \n")
        .append("    <form method=\"GET\" name=\"search_form\" url=\"{2}\" style=\"padding:5px;\"> \n")
        .append("      {3}                                                                         \n")
        .append("      {4}                                                                         \n")
        .append("      <div class=\"container\">                                                   \n")
        .append("        <div class=\"page\">                                                      \n")
        .append("          <ul>                                                                    \n")
        .append("            {5}                                                                   \n")
        .append("            <a href=\"javascript:void(0)\">{6}</a>                                \n")
        .append("            {7}                                                                   \n")
        .append("          </ul>                                                                   \n")
        .append("          ([ <b>{8}</b> ]records, [ <b>{9}</b> ]pages, [{10}]records/page)        \n")
        .append("        </div>                                                                    \n")
        .append("      </div>                                                                      \n")
        .append("    </form>                                                                       \n")
        .append("    {11}                                                                          \n")
        .append("  </body>                                                                         \n")
        .append("</html>                                                                           \n")
        .toString().replaceAll("\\s+\n", "\n");

    // -------------------------------------------------------------------------------
    private static final String INPUT_BOX = 
        "<input type=\"text\" name=\"{0}\" value=\"{1}\" style=\"width:40px;height:32px;text-align:center;margin:5px;font-weight:bold;\"/>";
    private static String buildInputBox(String name, Object value) {
        return MessageFormat.format(INPUT_BOX, name, value);
    }

    // -------------------------------------------------------------------------------
    private static final String PAGE_ARROW = 
        "<li><a href=\"{0}?pageNum={1}&pageSize={2}{3}\"></a></li>";
    private static String buildPageArrow(String url, int pageNum, int pageSize, 
                                         int totalPages, String params) {
        if (pageNum < 1 || pageNum > totalPages) {
            return EMPTY;
        }
        params = StringUtils.isBlank(params) ? "" : "&" + params;
        return MessageFormat.format(PAGE_ARROW, url, pageNum, pageSize, params);
    }

}
