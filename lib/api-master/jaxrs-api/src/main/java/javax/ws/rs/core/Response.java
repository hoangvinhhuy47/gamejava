//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package javax.ws.rs.core;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.Link.Builder;
import javax.ws.rs.ext.RuntimeDelegate;

public abstract class Response {
    protected Response() {
    }

    public abstract int getStatus();

    public abstract Response.StatusType getStatusInfo();

    public abstract Object getEntity();

    public abstract <T> T readEntity(Class<T> var1);

    public abstract <T> T readEntity(GenericType<T> var1);

    public abstract <T> T readEntity(Class<T> var1, Annotation[] var2);

    public abstract <T> T readEntity(GenericType<T> var1, Annotation[] var2);

    public abstract boolean hasEntity();

    public abstract boolean bufferEntity();

    public abstract void close();

    public abstract MediaType getMediaType();

    public abstract Locale getLanguage();

    public abstract int getLength();

    public abstract Set<String> getAllowedMethods();

    public abstract Map<String, NewCookie> getCookies();

    public abstract EntityTag getEntityTag();

    public abstract Date getDate();

    public abstract Date getLastModified();

    public abstract URI getLocation();

    public abstract Set<Link> getLinks();

    public abstract boolean hasLink(String var1);

    public abstract Link getLink(String var1);

    public abstract Builder getLinkBuilder(String var1);

    public abstract MultivaluedMap<String, Object> getMetadata();

    public MultivaluedMap<String, Object> getHeaders() {
        return this.getMetadata();
    }

    public abstract MultivaluedMap<String, String> getStringHeaders();

    public abstract String getHeaderString(String var1);

    public static Response.ResponseBuilder fromResponse(Response response) {
        Response.ResponseBuilder b = status(response.getStatus());
        if (response.hasEntity()) {
            b.entity(response.getEntity());
        }

        Iterator var2 = response.getHeaders().keySet().iterator();

        while(var2.hasNext()) {
            String headerName = (String)var2.next();
            List<Object> headerValues = (List)response.getHeaders().get(headerName);
            Iterator var5 = headerValues.iterator();

            while(var5.hasNext()) {
                Object headerValue = var5.next();
                b.header(headerName, headerValue);
            }
        }

        return b;
    }

    public static Response.ResponseBuilder status(Response.StatusType status) {
        return Response.ResponseBuilder.newInstance().status(status);
    }

    public static Response.ResponseBuilder status(Response.Status status) {
        return status((Response.StatusType)status);
    }

    public static Response.ResponseBuilder status(int status) {
        return Response.ResponseBuilder.newInstance().status(status);
    }

    public static Response.ResponseBuilder ok() {
        return status(Response.Status.OK);
    }

    public static Response.ResponseBuilder ok(Object entity) {
        Response.ResponseBuilder b = ok();
        b.entity(entity);
        return b;
    }

    public static Response.ResponseBuilder ok(Object entity, MediaType type) {
        return ok().entity(entity).type(type);
    }

    public static Response.ResponseBuilder ok(Object entity, String type) {
        return ok().entity(entity).type(type);
    }

    public static Response.ResponseBuilder ok(Object entity, Variant variant) {
        return ok().entity(entity).variant(variant);
    }

    public static Response.ResponseBuilder serverError() {
        return status(Response.Status.INTERNAL_SERVER_ERROR);
    }

    public static Response.ResponseBuilder created(URI location) {
        return status(Response.Status.CREATED).location(location);
    }

    public static Response.ResponseBuilder accepted() {
        return status(Response.Status.ACCEPTED);
    }

    public static Response.ResponseBuilder accepted(Object entity) {
        return accepted().entity(entity);
    }

    public static Response.ResponseBuilder noContent() {
        return status(Response.Status.NO_CONTENT);
    }

    public static Response.ResponseBuilder notModified() {
        return status(Response.Status.NOT_MODIFIED);
    }

    public static Response.ResponseBuilder notModified(EntityTag tag) {
        return notModified().tag(tag);
    }

    public static Response.ResponseBuilder notModified(String tag) {
        return notModified().tag(tag);
    }

    public static Response.ResponseBuilder seeOther(URI location) {
        return status(Response.Status.SEE_OTHER).location(location);
        }

    public static Response.ResponseBuilder temporaryRedirect(URI location) {
        return status(Response.Status.TEMPORARY_REDIRECT).location(location);
    }

    public static Response.ResponseBuilder notAcceptable(List<Variant> variants) {
        return status(Response.Status.NOT_ACCEPTABLE).variants(variants);
    }

    public static enum Status implements Response.StatusType {
        OK(200, "OK"),
        CREATED(201, "Created"),
        ACCEPTED(202, "Accepted"),
        NO_CONTENT(204, "No Content"),
        RESET_CONTENT(205, "Reset Content"),
        PARTIAL_CONTENT(206, "Partial Content"),
        MOVED_PERMANENTLY(301, "Moved Permanently"),
        FOUND(302, "Found"),
        SEE_OTHER(303, "See Other"),
        NOT_MODIFIED(304, "Not Modified"),
        USE_PROXY(305, "Use Proxy"),
        TEMPORARY_REDIRECT(307, "Temporary Redirect"),
        BAD_REQUEST(400, "Bad Request"),
        UNAUTHORIZED(401, "Unauthorized"),
        PAYMENT_REQUIRED(402, "Payment Required"),
        FORBIDDEN(403, "Forbidden"),
        NOT_FOUND(404, "Not Found"),
        METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
        NOT_ACCEPTABLE(406, "Not Acceptable"),
        PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
        REQUEST_TIMEOUT(408, "Request Timeout"),
        CONFLICT(409, "Conflict"),
        GONE(410, "Gone"),
        LENGTH_REQUIRED(411, "Length Required"),
        PRECONDITION_FAILED(412, "Precondition Failed"),
        REQUEST_ENTITY_TOO_LARGE(413, "Request Entity Too Large"),
        REQUEST_URI_TOO_LONG(414, "Request-URI Too Long"),
        UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
        REQUESTED_RANGE_NOT_SATISFIABLE(416, "Requested Range Not Satisfiable"),
        EXPECTATION_FAILED(417, "Expectation Failed"),
        INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
        NOT_IMPLEMENTED(501, "Not Implemented"),
        BAD_GATEWAY(502, "Bad Gateway"),
        SERVICE_UNAVAILABLE(503, "Service Unavailable"),
        GATEWAY_TIMEOUT(504, "Gateway Timeout"),
        HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported");

        private final int code;
        private final String reason;
        private final Response.Status.Family family;

        private Status(int statusCode, String reasonPhrase) {
            this.code = statusCode;
            this.reason = reasonPhrase;
            this.family = Response.Status.Family.familyOf(statusCode);
        }

        public Response.Status.Family getFamily() {
            return this.family;
        }

        public int getStatusCode() {
            return this.code;
        }

        public String getReasonPhrase() {
            return this.toString();
        }

        public String toString() {
            return this.reason;
        }

        public static Response.Status fromStatusCode(int statusCode) {
            Response.Status[] var1 = values();
            int var2 = var1.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                Response.Status s = var1[var3];
                if (s.code == statusCode) {
                    return s;
                }
            }

            return null;
        }

        public static enum Family {
            INFORMATIONAL,
            SUCCESSFUL,
            REDIRECTION,
            CLIENT_ERROR,
            SERVER_ERROR,
            OTHER;

            private Family() {
            }

            public static Response.Status.Family familyOf(int statusCode) {
                switch (statusCode / 100) {
                    case 1:
                    return INFORMATIONAL;
                    case 2:
                    return SUCCESSFUL;
                    case 3:
                    return REDIRECTION;
                    case 4:
                    return CLIENT_ERROR;
                    case 5:
                    return SERVER_ERROR;
                    default:
                    return OTHER;
                }
            }
        }
        }

    public interface StatusType {
        int getStatusCode();

        Response.Status.Family getFamily();

        String getReasonPhrase();
        }

    public abstract static class ResponseBuilder {
        protected ResponseBuilder() {
        }

        protected static Response.ResponseBuilder newInstance() {
            return RuntimeDelegate.getInstance().createResponseBuilder();
        }

        public abstract Response build();

        public abstract Response.ResponseBuilder clone();

        public abstract Response.ResponseBuilder status(int var1);

        public Response.ResponseBuilder status(Response.StatusType status) {
            if (status == null) {
                throw new IllegalArgumentException();
            } else {
                return this.status(status.getStatusCode());
                }
            }

        public Response.ResponseBuilder status(Response.Status status) {
            return this.status((Response.StatusType)status);
        }

        public abstract Response.ResponseBuilder entity(Object var1);

        public abstract Response.ResponseBuilder entity(NioWriterHandler var1);

        public abstract Response.ResponseBuilder entity(NioWriterHandler var1, NioErrorHandler var2);

        public abstract Response.ResponseBuilder entity(Object var1, Annotation[] var2);

        public abstract Response.ResponseBuilder allow(String... var1);

        public abstract Response.ResponseBuilder allow(Set<String> var1);

        public abstract Response.ResponseBuilder cacheControl(CacheControl var1);

        public abstract Response.ResponseBuilder encoding(String var1);

        public abstract Response.ResponseBuilder header(String var1, Object var2);

        public abstract Response.ResponseBuilder replaceAll(MultivaluedMap<String, Object> var1);

        public abstract Response.ResponseBuilder language(String var1);

        public abstract Response.ResponseBuilder language(Locale var1);

        public abstract Response.ResponseBuilder type(MediaType var1);

        public abstract Response.ResponseBuilder type(String var1);

        public abstract Response.ResponseBuilder variant(Variant var1);

        public abstract Response.ResponseBuilder contentLocation(URI var1);

        public abstract Response.ResponseBuilder cookie(NewCookie... var1);

        public abstract Response.ResponseBuilder expires(Date var1);

        public abstract Response.ResponseBuilder lastModified(Date var1);

        public abstract Response.ResponseBuilder location(URI var1);

        public abstract Response.ResponseBuilder tag(EntityTag var1);

        public abstract Response.ResponseBuilder tag(String var1);

        public abstract Response.ResponseBuilder variants(Variant... var1);

        public abstract Response.ResponseBuilder variants(List<Variant> var1);

        public abstract Response.ResponseBuilder links(Link... var1);

        public abstract Response.ResponseBuilder link(URI var1, String var2);

        public abstract Response.ResponseBuilder link(String var1, String var2);
    }
}
