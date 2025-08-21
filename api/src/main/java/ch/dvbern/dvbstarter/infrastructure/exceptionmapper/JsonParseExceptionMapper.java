package ch.dvbern.dvbstarter.infrastructure.exceptionmapper;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.json.AppFailureErrorJson;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import com.fasterxml.jackson.core.JsonParseException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Provider
@Slf4j
@RequiredArgsConstructor
class JsonParseExceptionMapper implements ExceptionMapper<JsonParseException> {
    private final TL tl;

    @Override
    public Response toResponse(JsonParseException exception) {
        var failureMessage = AppFailureMessage.jsonParsing(
            exception.getOriginalMessage(),
            exception.getLocation().offsetDescription()
        );
        var msg = tl.translate(failureMessage.getI18nMessage());

        return Response
            .status(Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .entity(new AppFailureErrorJson(
                failureMessage.getId(),
                msg
            ))
            .build();
    }

}
