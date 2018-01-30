package fr.xebia.ldi.fighter.stream.queries;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by loicmdivad.
 */
public class QueryUtil {

    public static final String BORDER_LEFT = "| ";
    private static final String BORDER_RIGHT = "| ";
    private static final String LINE = " -------------+-------------+-------------+------------- \n";

    public static String footer() {
        return LINE;
    }

    public static String header() {

        return LINE + BORDER_LEFT + Stream.of("concept", "window", "characters", "victories")

                .map((String cell) -> String.format("%1$-" + 12 + "s", cell) + BORDER_RIGHT)

                .collect((Collectors.joining(""))) + "\n" + LINE;
    }
}
