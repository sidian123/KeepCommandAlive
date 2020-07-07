package live.sidian.command.keepalive;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author sidian
 * @date 2020/7/7 19:05
 */
@Data
@Component
@ConfigurationProperties(prefix = "command")
public class Command {
    private String run;
    private String exits;

    public int[] getExits() {
        return Arrays.stream(exits.split(","))
                .filter(s -> !s.equals(""))
                .mapToInt(Integer::parseInt)
                .toArray();
    }
}
