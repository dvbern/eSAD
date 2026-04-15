package ch.dvbern.oss.hemed.esc.api.shared.printer;

import java.util.function.Function;

public record PrintUtilRecord<T>(String titleKey, boolean isVisible, Function<T, String> stringTransformer) { }
