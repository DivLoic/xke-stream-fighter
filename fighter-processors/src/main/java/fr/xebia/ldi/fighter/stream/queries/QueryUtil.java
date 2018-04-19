package fr.xebia.ldi.fighter.stream.queries;

import fr.xebia.ldi.fighter.schema.VictoriesCount;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.xebia.ldi.fighter.stream.utils.Parsing.printWindowStart;

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

    public static Stream<CharSequence> streamLine(VictoriesCount count) {
        return Stream.of(
                count.getConcept(),
                printWindowStart(count.getWindowStart()),
                count.getCharacter(),
                count.getVictories().toString()
        );
    }

    public static String line(VictoriesCount count){
        String content = streamLine(count)
                .map(Object::toString)
                .map((String cell) -> String.format("%1$-" + 12 + "s", cell))
                .collect((Collectors.joining(BORDER_LEFT)));

        return BORDER_LEFT + content + BORDER_RIGHT;
    }
}
