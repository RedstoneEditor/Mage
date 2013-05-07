import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import tk.redwirepvp.kitapi.api.Kit;

public class Mage extends Kit implements Listener {
	public FireworkEffectPlayer fep = new FireworkEffectPlayer();

	private HashMap<String, HashMap<Spell, Float>> xplvls = new HashMap<String, HashMap<Spell, Float>>();

	@SuppressWarnings("deprecation")
	public void giveKit(Player p) {
		PlayerInventory pi = p.getInventory();
		p.getInventory().clear();
		p.getInventory().setArmorContents(null);
		// chestplate
		ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
		ItemUtil.setColor(chest, 0x927AA9);
		chest.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
		pi.setChestplate(chest);
		// leggings
		ItemStack legs = new ItemStack(Material.LEATHER_LEGGINGS, 1);
		ItemUtil.setColor(legs, 0x927AA9);
		legs.addEnchantment(Enchantment.PROTECTION_FIRE, 1);
		pi.setLeggings(legs);
		// boots
		ItemStack boots = new ItemStack(Material.LEATHER_BOOTS, 1);
		ItemUtil.setColor(boots, 0x927AA9);
		boots.addEnchantment(Enchantment.PROTECTION_FALL, 1);
		pi.setBoots(boots);
		// damage spell
		ItemStack damage = new ItemStack(Material.DIAMOND_HOE, 1);
		ItemUtil.setName(damage, ChatColor.DARK_PURPLE + "Damage Spell");
		pi.addItem(damage);
		// fire spell
		ItemStack fire = new ItemStack(Material.WOOD_HOE, 1);
		ItemUtil.setName(fire, ChatColor.RED + "Fire Spell");
		pi.addItem(fire);
		// lightning spell
		ItemStack thor = new ItemStack(Material.STONE_HOE, 1);
		ItemUtil.setName(thor, ChatColor.YELLOW + "Lightning Spell");
		pi.addItem(thor);
		// freeze spell
		ItemStack freeze = new ItemStack(Material.IRON_HOE, 1);
		ItemUtil.setName(freeze, ChatColor.BLUE + "Freeze Spell");
		pi.addItem(freeze);
		// heal spell
		ItemStack heal = new ItemStack(Material.GOLD_HOE, 1);
		ItemUtil.setName(heal, ChatColor.GREEN + "Heal Spell");
		pi.addItem(heal);
		p.updateInventory();
		p.sendMessage(ChatColor.GREEN + "You are now a Mage");
		p.setExp(1F);

	}

	public void fireArrows(World world, Player player) {
		Projectile arrow = player.launchProjectile(Arrow.class);
		Vector vec = player.getLocation().getDirection();
		arrow.setVelocity(new Vector(vec.getX() * 5, vec.getY() * 5,
				vec.getZ() * 5));
	}

	public int xpid;

	public HashMap<Arrow, Boolean> arrows = new HashMap<Arrow, Boolean>();

	@EventHandler
	public void onProjectileLaunch(final ProjectileLaunchEvent event) {
		if (this.plugin.players.get((Player) event.getEntity().getShooter()) == getName()) {
			if (event.getEntity() instanceof Arrow) {
				arrows.put((Arrow) event.getEntity(), false);
				setExp((Player) event.getEntity().getShooter(), Spell.DAMAGE, 0F);
				xpid = Bukkit.getScheduler().scheduleSyncRepeatingTask(
						this.plugin, new Runnable() {
							public void run() {
								if (getExp((Player) event.getEntity()
										.getShooter(), Spell.DAMAGE) < 1F) {
									setExp((Player) event.getEntity()
											.getShooter(),
											Spell.DAMAGE,
											getExp((Player) event.getEntity()
													.getShooter(), Spell.DAMAGE) + 0.05F);
								} else {
									Bukkit.getScheduler().cancelTask(xpid);
									((Player) event.getEntity().getShooter())
											.setExp(1F);
								}
							}
						}, 0L, 1L);
				Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin,
						new Runnable() {
							public void run() {
								if (arrows.containsKey(event.getEntity())
										&& arrows.get(event.getEntity()) == false) {
									event.getEntity().remove();
									playFirework(event.getEntity().getWorld(),
											event.getEntity().getLocation());
								}
							}
						}, 5L);
			}
		}
	}

	@EventHandler
	public void rightClick(PlayerInteractEvent event) {
		if (this.plugin.players.get(event.getPlayer().getName()) == getName())
			if (event.getAction() == Action.RIGHT_CLICK_AIR
					|| event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if (event.getPlayer().getItemInHand().getType() == Material.DIAMOND_HOE) {
					if (event.getPlayer().getExp() >= 1F) {
						event.setCancelled(true);
						fireArrows(event.getPlayer().getWorld(),
								event.getPlayer());
					}
				}
			}
	}

	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		Entity entity = event.getEntity();
		System.out.println(((Arrow) entity).getShooter());
		if (entity instanceof Arrow) {
			if (((Arrow) entity).getShooter() instanceof Player) {
				Player player = (Player) ((Arrow) entity).getShooter();
				if (plugin.players
						.get(((Player) event.getEntity().getShooter())
								.getName()) == getName()) {
					System.out.println(player.getName());
					Arrow arrow = (Arrow) event.getEntity();
					arrow.remove();
					arrows.remove(arrow);
					playFirework(event.getEntity().getWorld(), event
							.getEntity().getLocation());
				}
			}
		}
	}

	private void playFirework(World world, Location location) {
		try {
			fep.playFirework(world, location, getRandomEffect());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static FireworkEffect getRandomEffect() {
		return FireworkEffect.builder().with(Type.BALL).withColor(Color.RED)
				.withColor(Color.PURPLE).build();
	}

	@Override
	public String getAuthor() {
		// TODO Auto-generated method stub
		return "RedstoneEditor";
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Mage";
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return "1.0";
	}

	@Override
	public void onDisable() {
		ProjectileHitEvent.getHandlerList().unregister(this);
		PlayerInteractEvent.getHandlerList().unregister(this);
		ProjectileLaunchEvent.getHandlerList().unregister(this);

	}

	@Override
	public void onEnable() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable(){
			public void run(){
				for(Player p : plugin.getServer().getOnlinePlayers()){
					if(plugin.players.containsKey(p.getName())){
						if(plugin.players.get(p.getName()) == getName()){
							switch(p.getItemInHand().getType()){
							case DIAMOND_HOE:
								p.setExp(getExp(p, Spell.DAMAGE));
								break;
							case WOOD_HOE:
								p.setExp(getExp(p, Spell.FLAME));
								break;
							case STONE_HOE:
								p.setExp(getExp(p, Spell.THOR));
								break;
							case IRON_HOE:
								p.setExp(getExp(p, Spell.FREEZE));
								break;
							case GOLD_HOE:
								p.setExp(getExp(p, Spell.HEAL));
								break;
							}
						}
					}
				}
			}
		}, 0L, 20L);
	}

	public void setExp(Player p, Spell spell, Float value) {

		if (xplvls.get(p.getName()).containsKey(spell)) {
			xplvls.get(p.getName()).remove(spell);
		}
		xplvls.get(p.getName()).put(spell, value);
	}

	public float getExp(Player p, Spell spell) {
		return xplvls.get(p.getName()).get(spell);
	}
	
	public enum Spell{
		DAMAGE, FLAME, THOR, FREEZE, HEAL
	}

}
