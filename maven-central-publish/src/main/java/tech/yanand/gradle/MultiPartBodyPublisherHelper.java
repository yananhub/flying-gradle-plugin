package tech.yanand.gradle;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.net.http.HttpRequest.BodyPublisher;

import static java.net.http.HttpRequest.BodyPublishers.concat;
import static java.net.http.HttpRequest.BodyPublishers.ofFile;
import static java.net.http.HttpRequest.BodyPublishers.ofString;

class MultiPartBodyPublisherHelper {

    private static final String CRLF = "\r\n";

    private MultiPartBodyPublisherHelper() {
        // Instantiation is not allowed.
    }

    static BodyPublisher getFilePartPublisher(String boundary, Path file) throws FileNotFoundException {
        String boundaryStart = CRLF + "--" + boundary + CRLF;
        String boundaryEnd = CRLF + "--" + boundary + "--";
        String partMeta = getFilePartMeta(boundaryStart, file.getFileName());
        return concat(ofString(partMeta), ofFile(file), ofString(boundaryEnd));
    }

    private static String getFilePartMeta(String boundaryStart, Path fileName) {
        return new StringBuilder()
                .append(boundaryStart)
                .append("Content-Disposition: form-data; name=\"bundle\"; filename=\"").append(fileName).append("\"").append(CRLF)
                .append("Content-Type: application/octet-stream").append(CRLF)
                .append(CRLF)
                .toString();
    }
}
