package tech.yanand.gradle.mavenpublish;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.http.HttpRequest.BodyPublisher;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.Flow;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static tech.yanand.gradle.mavenpublish.MultiPartBodyPublisherHelper.CRLF;

class MultiPartBodyPublisherHelperTest {

    private static final String BODY = CRLF +
            "--%s" + CRLF +
            "Content-Disposition: form-data; name=\"bundle\"; filename=\"%s\"" + CRLF +
            "Content-Type: application/octet-stream" + CRLF +
            CRLF +
            "File_content" + CRLF +
            "--%s--";

    @TempDir
    private Path tempDir;

    @Test
    void getFilePartPublisher_checkBody() throws IOException {
        String boundary = UUID.randomUUID().toString().replace("-", "");
        Path uploadFile = Files.createTempFile(tempDir, "test_bundle", ".zip");
        Files.writeString(uploadFile, "File_content");

        BodyPublisher bodyPublisher = MultiPartBodyPublisherHelper.getFilePartPublisher(boundary, uploadFile);
        String actual = asString(bodyPublisher);

        assertEquals(format(BODY, boundary, uploadFile.getFileName(), boundary), actual);
    }

    private String asString(BodyPublisher publisher) {
        StringSubscriber subscriber = new StringSubscriber();
        publisher.subscribe(subscriber);
        return subscriber.resultString;
    }

    private static class StringSubscriber implements Flow.Subscriber<ByteBuffer> {

        private String resultString = "";

        public void onSubscribe(Flow.Subscription subscription) {
            subscription.request(3);
        }

        public void onNext(ByteBuffer item) {
            resultString += StandardCharsets.UTF_8.decode(item).toString();
        }

        public void onError(Throwable ex) {
            fail(ex);
        }

        public void onComplete() {
        }
    }
}