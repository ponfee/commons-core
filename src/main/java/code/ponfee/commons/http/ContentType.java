package code.ponfee.commons.http;

/**
 * Representing http 'Content-Type' header
 * 
 * @author Ponfee
 */
public enum ContentType {

    APPLICATION_FORM_URLENCODED("application/x-www-form-urlencoded"), //
    APPLICATION_JSON("application/json"), //
    APPLICATION_OCTET_STREAM("application/octet-stream"), //
    APPLICATION_XML("application/xml"), //
    APPLICATION_ATOM_XML("application/atom+xml"), //
    APPLICATION_SVG_XML("application/svg+xml"), //
    APPLICATION_XHTML_XML("application/xhtml+xml"), //

    MULTIPART_FORM_DATA("multipart/form-data"), //

    TEXT_XML("text/xml"), //
    TEXT_HTML("text/html"), //
    TEXT_PLAIN("text/plain"), //

    IMAGE_JPEG("image/jpeg"), //
    IMAGE_PNG("image/png"), //
    IMAGE_BMP("image/bmp"), //
    IMAGE_ICO("image/ico"), //
    IMAGE_GIF("image/gif"), //

    WILDCARD("*/*"), //
    ;

    final String value;

    ContentType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static ContentType ofValue(String value) {
        for (ContentType type : ContentType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }

}
