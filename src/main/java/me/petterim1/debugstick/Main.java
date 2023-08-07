package me.petterim1.debugstick;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemStick;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.plugin.PluginBase;

public class Main extends PluginBase implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equals("debugstick")) {
            if (!sender.hasPermission("debugstick.give")) {
                return false;
            }

            Player target;
            if (sender instanceof Player && args.length == 0) {
                target = (Player) sender;
            } else if (args.length > 0) {
                target = getServer().getPlayerExact(args[0]);

                if (target != sender && !sender.hasPermission("debugstick.give.others")) {
                    return false;
                }

                if (target == null) {
                    sender.sendMessage("Unknown player: " + args[0]);
                    return true;
                }
            } else {
                return false;
            }

            Item stick = Item.get(Item.STICK, 0, 1);
            stick.setNamedTag(new CompoundTag().putBoolean("debug_stick", true));
            stick.addEnchantment(Enchantment.get(-1));
            stick.setCustomName("§eDebug Stick");

            if (target.getInventory().addItem(stick).length == 0) {
                sender.sendMessage("Gave debug stick to " + target.getName());
            } else {
                sender.sendMessage(target.getName() + " has no space in inventory");
            }
            return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerInteractEvent event) {
        if (event.getAction() != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || !(event.getItem() instanceof ItemStick)) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (player.isCreative() && !player.isSneaking() && event.getItem().getNamedTag().getBoolean("debug_stick") && player.hasPermission("debugstick.use")) {
            event.setCancelled(true);
            
            int newDamage = block.getDamage() + 1;
            if (newDamage > 15) {
                newDamage = 0;
            }

            block.setDamage(newDamage);
            block.getLevel().setBlock(block, block, true, false);

            player.sendActionBar(block.getId() + ":" + block.getDamage());
        }
    }
}
