package me.HeyAwesomePeople.Blocks.database;

import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.util.UUID;

public class Redis {

    private RMap<String, Integer> blocks;
    private RMap<String, Integer> cubes;

    public Redis() {
        RedissonClient client = Redisson.create();

        blocks = client.getMap("blocks_blocks");
        cubes = client.getMap("blocks_cubes");
    }

    public boolean isInCache(UUID id) {
        return blocks.containsKey(id.toString()) && cubes.containsKey(id.toString());
    }

    public Integer getBlocks(UUID id) {
        return blocks.get(id.toString());
    }

    public void setBlocks(UUID id, Integer blocks) {
        this.blocks.fastPut(id.toString(), blocks);
    }

    public Integer getCubes(UUID id){
        return cubes.get(id.toString());
    }

    public void setCubes(UUID id, Integer cubes) {
        this.cubes.fastPut(id.toString(), cubes);
    }

}
