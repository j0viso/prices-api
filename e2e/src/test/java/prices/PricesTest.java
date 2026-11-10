package prices;

import io.karatelabs.core.Runner;
import io.karatelabs.core.SuiteResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class PricesTest {

    @Test
    void everyFeature() {
        SuiteResult results = Runner.path("src/test/java/prices")
                .outputJunitXml(true)
                .parallel(4);

        assertFalse(results.isFailed(), String.join("\n", results.getErrors()));
    }
}
