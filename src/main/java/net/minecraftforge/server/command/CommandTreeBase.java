package net.minecraftforge.server.command;

import committee.nova.util.CommandUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatMessageComponent;

import java.util.*;

/**
 * Base class for commands that has subcommands.
 * <p>
 * E.g. /team settings set [value]
 * settings is subcommand of team and set is subcommand of settings
 */
@SuppressWarnings("unchecked")
public abstract class CommandTreeBase extends CommandBase {
    private final Map<String, ICommand> commandMap = new HashMap<String, ICommand>();
    private final Map<String, ICommand> commandAliasMap = new HashMap<String, ICommand>();

    public void addSubcommand(ICommand command) {
        commandMap.put(command.getCommandName(), command);
        for (String alias : (List<String>) command.getCommandAliases()) commandAliasMap.put(alias, command);
    }

    public Collection<ICommand> getSubCommands() {
        return commandMap.values();
    }

    public ICommand getSubCommand(String command) {
        final ICommand cmd = commandMap.get(command);
        if (cmd != null) return cmd;
        return commandAliasMap.get(command);
    }

    public Map<String, ICommand> getCommandMap() {
        return Collections.unmodifiableMap(commandMap);
    }

    public List<ICommand> getSortedCommandList() {
        final List<ICommand> list = new ArrayList<ICommand>(getSubCommands());
        Collections.sort(list);
        return list;
    }

    private static String[] shiftArgs(String[] s) {
        if (s == null || s.length == 0) return new String[0];
        final String[] s1 = new String[s.length - 1];
        System.arraycopy(s, 1, s1, 0, s1.length);
        return s1;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            final List<String> keys = new ArrayList<String>();
            for (ICommand c : getSubCommands()) if (c.canCommandSenderUseCommand(sender)) keys.add(c.getCommandName());
            CommandUtils.sort(keys, null);
            return CommandUtils.getListOfStringsMatchingLastWord(args, keys);
        }
        final ICommand cmd = getSubCommand(args[0]);
        if (cmd != null) return cmd.addTabCompletionOptions(sender, shiftArgs(args));
        return super.addTabCompletionOptions(sender, args);
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        if (index > 0 && args.length > 1) {
            final ICommand cmd = getSubCommand(args[0]);
            if (cmd != null) return cmd.isUsernameIndex(shiftArgs(args), index - 1);
        }
        return false;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            final String subCommandsString = getAvailableSubCommandsString(sender);
            sender.sendChatToPlayer(ChatMessageComponent.createFromTranslationWithSubstitutions("commands.tree_base.available_subcommands", subCommandsString));
        } else {
            final ICommand cmd = getSubCommand(args[0]);
            if (cmd == null) {
                final String subCommandsString = getAvailableSubCommandsString(sender);
                throw new CommandException("commands.tree_base.invalid_cmd.list_subcommands", args[0], subCommandsString);
            } else if (!cmd.canCommandSenderUseCommand(sender)) {
                throw new CommandException("commands.generic.permission");
            } else {
                cmd.processCommand(sender, shiftArgs(args));
            }
        }
    }

    private String getAvailableSubCommandsString(ICommandSender sender) {
        final Collection<String> availableCommands = new ArrayList<String>();
        for (ICommand command : getSubCommands())
            if (command.canCommandSenderUseCommand(sender)) availableCommands.add(command.getCommandName());
        return CommandUtils.joinNiceStringFromCollection(availableCommands);
    }
}
