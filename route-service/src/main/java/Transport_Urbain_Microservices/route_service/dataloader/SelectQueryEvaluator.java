package Transport_Urbain_Microservices.route_service.dataloader;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.boolex.EventEvaluatorBase;

public class SelectQueryEvaluator extends EventEvaluatorBase<ILoggingEvent> {
    @Override
    public boolean evaluate(ILoggingEvent event) throws NullPointerException, EvaluationException {
        String message = event.getMessage();
        return message != null && message.trim().toLowerCase().startsWith("select");
    }
}