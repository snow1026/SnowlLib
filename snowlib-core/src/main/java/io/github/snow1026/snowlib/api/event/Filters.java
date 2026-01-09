package io.github.snow1026.snowlib.api.event;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Predicate;

/**
 * 이벤트 처리에 유용한 공통 필터들을 제공하는 유틸리티 클래스입니다.
 */
public final class Filters {
    private Filters() {}

    /**
     * 취소된 이벤트를 걸러내는 필터입니다.
     * <p>취소되지 않은 이벤트만 통과시킵니다.</p>
     *
     * @param <T> 이벤트 타입
     * @return 필터 Predicate
     */
    public static <T extends Event> Predicate<T> ignoreCancelled() {
        return e -> !(e instanceof Cancellable c) || !c.isCancelled();
    }

    /**
     * 이벤트의 주체가 플레이어인 경우만 통과시킵니다.
     * PlayerEvent 혹은 EntityEvent(주체가 Player인 경우)를 처리합니다.
     *
     * @param <T> 이벤트 타입
     * @return 필터 Predicate
     */
    public static <T extends Event> Predicate<T> isPlayer() {
        return e -> {
            if (e instanceof PlayerEvent) return true;
            return e instanceof EntityEvent ee && ee.getEntity() instanceof Player;
        };
    }

    /**
     * 이벤트 주체(플레이어)가 특정 권한을 가지고 있는지 확인합니다.
     * 플레이어 관련 이벤트가 아닐 경우 false를 반환합니다.
     *
     * @param permission 확인할 권한 노드
     * @param <T>        이벤트 타입
     * @return 필터 Predicate
     */
    public static <T extends Event> Predicate<T> hasPermission(String permission) {
        return e -> {
            Player p = getPlayer(e);
            return p != null && p.hasPermission(permission);
        };
    }

    /**
     * 플레이어가 손에 특정 아이템을 들고 있는지 확인합니다.
     * (메인 핸드 기준)
     *
     * @param material 확인할 아이템의 재질
     * @param <T>      이벤트 타입
     * @return 필터 Predicate
     */
    public static <T extends Event> Predicate<T> handIs(Material material) {
        return e -> {
            Player p = getPlayer(e);
            if (p == null) return false;
            ItemStack item = p.getInventory().getItemInMainHand();
            return item.getType() == material;
        };
    }

    /**
     * 이벤트와 관련된 블록이 특정 재질(Material)인지 확인합니다.
     * <p>
     * {@link BlockEvent} 뿐만 아니라 {@link PlayerInteractEvent}, {@link ProjectileHitEvent} 등
     * 블록을 대상으로 하는 주요 이벤트들을 자동으로 감지하여 처리합니다.
     * </p>
     *
     * @param material 확인할 블록의 재질
     * @param <T>      이벤트 타입
     * @return 필터 Predicate
     */
    public static <T extends Event> Predicate<T> blockIs(Material material) {
        return e -> {
            Block block = null;

            // 1. 일반적인 블록 이벤트 (파괴, 설치, 번짐 등)
            if (e instanceof BlockEvent be) {
                block = be.getBlock();
            }
            // 2. 플레이어 상호작용 (우클릭, 좌클릭 등)
            else if (e instanceof PlayerInteractEvent pie) {
                block = pie.getClickedBlock();
            }
            // 3. 투사체(화살, 눈덩이 등)가 블록에 맞았을 때
            else if (e instanceof ProjectileHitEvent phe) {
                block = phe.getHitBlock();
            }
            // 4. 엔티티가 블록을 변경할 때 (엔더맨, 양이 잔디 먹음, 블록 낙하 등)
            else if (e instanceof EntityChangeBlockEvent ecbe) {
                block = ecbe.getBlock();
            }

            // 블록이 존재하고, 그 타입이 일치하는지 확인
            return block != null && block.getType() == material;
        };
    }

    // 내부 유틸: 이벤트에서 플레이어 추출
    private static Player getPlayer(Event e) {
        if (e instanceof PlayerEvent pe) return pe.getPlayer();
        if (e instanceof EntityEvent ee && ee.getEntity() instanceof Player p) return p;
        return null;
    }
}
