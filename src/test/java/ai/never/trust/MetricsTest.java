package ai.never.trust;

import ai.never.trust.metric.*;
import ai.never.trust.model.TestResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MetricsTest {

    private TestResult result(double score) {
        TestResult r = new TestResult();
        r.setConsensusScore(score);
        r.setPassed(score >= 0.7);
        return r;
    }

    @Test
    void accuracyAllPass() {
        List<TestResult> results = List.of(result(0.9), result(0.8), result(0.7), result(0.6));
        double score = new AccuracyMetric().calculate(results);
        assertEquals(1.0, score, 0.001); // all >= 0.5
    }

    @Test
    void accuracyHalfPass() {
        List<TestResult> results = List.of(result(0.8), result(0.8), result(0.3), result(0.3));
        double score = new AccuracyMetric().calculate(results);
        assertEquals(0.5, score, 0.001);
    }

    @Test
    void consistencyPerfect() {
        List<TestResult> results = List.of(result(0.8), result(0.8), result(0.8), result(0.8));
        double score = new ConsistencyMetric().calculate(results);
        assertEquals(1.0, score, 0.001);
    }

    @Test
    void consistencyLow() {
        List<TestResult> results = List.of(result(0.0), result(1.0), result(0.0), result(1.0));
        double score = new ConsistencyMetric().calculate(results);
        assertTrue(score < 0.1); // high variance = low consistency
    }

    @Test
    void hallucinationNone() {
        List<TestResult> results = List.of(result(0.8), result(0.9), result(0.7));
        double score = new HallucinationMetric().calculate(results);
        assertEquals(1.0, score, 0.001);
    }

    @Test
    void hallucinationAll() {
        List<TestResult> results = List.of(result(0.1), result(0.2), result(0.0));
        double score = new HallucinationMetric().calculate(results);
        assertEquals(0.0, score, 0.001);
    }

    @Test
    void incidentNone() {
        List<TestResult> results = List.of(result(0.8), result(0.9), result(0.95));
        double score = new IncidentMetric().calculate(results);
        assertEquals(1.0, score, 0.001);
    }

    @Test
    void incidentAll() {
        List<TestResult> results = List.of(result(0.1), result(0.2), result(0.3));
        double score = new IncidentMetric().calculate(results); // threshold 0.7
        assertEquals(0.0, score, 0.001);
    }
}
