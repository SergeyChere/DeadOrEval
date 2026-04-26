package ai.never.trust.model.warmup;

/**
 * Represents a question used to calibrate judges before evaluation begins.
 * WarmupQuestions have known correct answers, allowing DeadOrEval to measure
 * how accurately each judge evaluates responses.
 * The more accurate a judge is on warmup questions,
 * the higher its weight in the final consensus score.
 *
 * @param context        The background context provided to the judge.
 *                       Describes the situation the agent is operating in.
 *                       Example: "A client is calling a dental clinic to schedule an appointment."
 *
 * @param userQuery      The question or message sent to the agent during warmup.
 *                       Example: "What are your opening hours?"
 *
 * @param expectedOutput The correct expected response for this warmup question.
 *                       Used to measure judge accuracy.
 *                       Example: "We are open Monday to Friday, 9am to 5pm."
 */
public record WarmupQuestion(
    String context,
    String userQuery,
    String expectedOutput
) {}
