package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import lombok.Getter;
import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public abstract class AbstractFenceConnectionHandler implements ConnectionHandler {
    private final String blockConnections;
    @Getter
    private HashSet<Integer> blockStates = new HashSet<>();
    private Map<Byte, Integer> connectedBlockStates = new HashMap<>();

    public AbstractFenceConnectionHandler(String blockConnections, String key){
        this.blockConnections = blockConnections;

        for (Map.Entry<String, Integer> blockState : ConnectionData.keyToId.entrySet()) {
            if (key.equals(blockState.getKey().split("\\[")[0])) {
                blockStates.add(blockState.getValue());
                ConnectionData.connectionHandlerMap.put(blockState.getValue(), this);
                WrappedBlockData blockData = WrappedBlockData.fromString(blockState.getKey());
                connectedBlockStates.put(getStates(blockData), blockState.getValue());
            }
        }
    }

    protected Byte getStates(WrappedBlockData blockData) {
        byte states = 0;
        if (blockData.getValue("east").equals("true")) states |= 1;
        if (blockData.getValue("north").equals("true")) states |= 2;
        if (blockData.getValue("south").equals("true")) states |= 4;
        if (blockData.getValue("west").equals("true")) states |= 8;
        if (blockData.getValue("waterlogged").equals("true")) states |= 16;
        return states;
    }

    protected Byte getStates(Position position, int blockState, ConnectionData connectionData) {
        byte states = 0;
        if (connects(BlockFace.EAST, connectionData.get(position.getRelative(BlockFace.EAST)))) states |= 1;
        if (connects(BlockFace.NORTH, connectionData.get(position.getRelative(BlockFace.NORTH)))) states |= 2;
        if (connects(BlockFace.SOUTH, connectionData.get(position.getRelative(BlockFace.SOUTH)))) states |= 4;
        if (connects(BlockFace.WEST, connectionData.get(position.getRelative(BlockFace.WEST)))) states |= 8;
        return states;
    }

    @Override
    public int connect(Position position, int blockState, ConnectionData connectionData) {
        final Integer newBlockState = connectedBlockStates.get(getStates(position, blockState, connectionData));
        return newBlockState == null ? blockState : newBlockState;
    }

    protected boolean connects(BlockFace side, int blockState) {
        return blockStates.contains(blockState) || ConnectionData.blockConnectionData.containsKey(blockState) && ConnectionData.blockConnectionData.get(blockState).connectTo(blockConnections, side.opposite());
    }
}
