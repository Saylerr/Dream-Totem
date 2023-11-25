package cc.dreamcode.totem.command;

import cc.dreamcode.command.annotations.RequiredPlayer;
import cc.dreamcode.command.bukkit.BukkitCommand;
import cc.dreamcode.totem.TotemService;
import eu.okaeri.injector.annotation.Inject;
import lombok.NonNull;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@RequiredPlayer
public class TotemCommand extends BukkitCommand {

    private final TotemService totemService;

    @Inject
    public TotemCommand(final TotemService totemService) {
        super("totem");

        this.totemService = totemService;
    }

    @Override
    public void content(@NonNull CommandSender sender, @NonNull String[] args) {
        final Player player = (Player) sender;
        totemService.getEffectsMenu().open(player);
    }

    @Override
    public List<String> tab(@NonNull CommandSender sender, @NonNull String[] args) {
        return null;
    }
}
