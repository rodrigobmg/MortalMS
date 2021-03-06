/*
    This file is part of the HeavenMS MapleStory Server, commands OdinMS-based
    Copyleft (L) 2016 - 2018 RonanLana

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/*
   @Author: Arthur L - Refactored command content into modules
*/
package client.command.commands.gm5;

import client.MapleCharacter;
import client.MapleClient;
import client.command.Command;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import net.server.Server;
import server.MaplePortal;
import server.TimerManager;
import server.life.MapleMonster;
import server.life.SpawnPoint;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleReactor;
import tools.MaplePacketCreator;

public class DebugCommand extends Command {
    private static final String[] debugTypes = {
        "monster",
        "packet",
        "portal",
        "spawnpoint",
        "pos",
        "map",
        "mobsp",
        "event",
        "areas",
        "reactors",
        "servercoupons",
        "playercoupons",
        "timer",
        "marriage",
        ""
    };

    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();

        if (params.length < 1) {
            player.yellowMessage("Syntax: !debug <type>");
            return;
        }

        switch (params[0]) {
            case "type":
            case "help":
                StringBuilder msgTypes = new StringBuilder("Available #bdebug types#k:\n\n");
                for (int i = 0; i < debugTypes.length; ++i) {
                    msgTypes.append("#L")
                            .append(i)
                            .append('#')
                            .append(debugTypes[i])
                            .append("#l\n");
                }

                player.announce(
                        MaplePacketCreator.getNPCTalk(
                                9201143, (byte) 0, msgTypes.toString(), "00 00", (byte) 0));
                break;

            case "monster":
                List<MapleMapObject> monsters =
                        player.getMap()
                                .getMapObjectsInRange(
                                        player.getPosition(),
                                        Double.POSITIVE_INFINITY,
                                        Collections.singletonList(MapleMapObjectType.MONSTER));
                for (MapleMapObject monstermo : monsters) {
                    MapleMonster monster = (MapleMonster) monstermo;
                    player.message(
                            "Monster ID: "
                                    + monster.getId()
                                    + " Aggro target: "
                                    + (monster.getController() != null
                                            ? monster.getController().getName()
                                            : "<none>"));
                }
                break;

            case "packet":
                player.getMap()
                        .broadcastMessage(
                                MaplePacketCreator.customPacket(joinStringFrom(params, 1)));
                break;

            case "portal":
                MaplePortal portal = player.getMap().findClosestPortal(player.getPosition());
                if (portal != null)
                    player.dropMessage(
                            6,
                            "Closest portal: "
                                    + portal.getId()
                                    + " '"
                                    + portal.getName()
                                    + "' Type: "
                                    + portal.getType()
                                    + " --> toMap: "
                                    + portal.getTargetMapId()
                                    + " scriptname: '"
                                    + portal.getScriptName()
                                    + "' state: "
                                    + portal.getPortalState()
                                    + '.');
                else player.dropMessage(6, "There is no portal on this map.");
                break;

            case "spawnpoint":
                SpawnPoint sp = player.getMap().findClosestSpawnpoint(player.getPosition());
                if (sp != null)
                    player.dropMessage(
                            6,
                            "Closest mob spawn point: "
                                    + " Position: x "
                                    + sp.getPosition().getX()
                                    + " y "
                                    + sp.getPosition().getY()
                                    + " Spawns mobid: '"
                                    + sp.getMonsterId()
                                    + "' --> canSpawn: "
                                    + !sp.getDenySpawn()
                                    + " canSpawnRightNow: "
                                    + sp.shouldSpawn()
                                    + '.');
                else player.dropMessage(6, "There is no mob spawn point on this map.");
                break;

            case "pos":
                player.dropMessage(
                        6,
                        "Current map position: ("
                                + player.getPosition().getX()
                                + ", "
                                + player.getPosition().getY()
                                + ").");
                break;

            case "map":
                player.dropMessage(
                        6,
                        "Current map id "
                                + player.getMap().getId()
                                + ", event: '"
                                + ((player.getMap().getEventInstance() != null)
                                        ? player.getMap().getEventInstance().getName()
                                        : "null")
                                + "'; Players: "
                                + player.getMap().getAllPlayers().size()
                                + ", Mobs: "
                                + player.getMap().countMonsters()
                                + ", Reactors: "
                                + player.getMap().countReactors()
                                + ", Items: "
                                + player.getMap().countItems()
                                + ", Objects: "
                                + player.getMap().getMapObjects().size()
                                + '.');
                break;

            case "mobsp":
                player.getMap().reportMonsterSpawnPoints(player);
                break;

            case "event":
                if (player.getEventInstance() == null)
                    player.dropMessage(6, "Player currently not in an event.");
                else
                    player.dropMessage(
                            6, "Current event name: " + player.getEventInstance().getName() + '.');
                break;

            case "areas":
                player.dropMessage(6, "Configured areas on map " + player.getMapId() + ':');

                byte index = 0;
                for (Rectangle rect : player.getMap().getAreas()) {
                    player.dropMessage(
                            6,
                            "Id: "
                                    + index
                                    + " -> posX: "
                                    + rect.getX()
                                    + " posY: '"
                                    + rect.getY()
                                    + "' dX: "
                                    + rect.getWidth()
                                    + " dY: "
                                    + rect.getHeight()
                                    + '.');
                    index++;
                }
                break;

            case "reactors":
                player.dropMessage(6, "Current reactor states on map " + player.getMapId() + ':');

                for (MapleMapObject mmo : player.getMap().getReactors()) {
                    MapleReactor mr = (MapleReactor) mmo;
                    player.dropMessage(
                            6,
                            "Id: "
                                    + mr.getId()
                                    + " Oid: "
                                    + mr.getObjectId()
                                    + " name: '"
                                    + mr.getName()
                                    + "' -> Type: "
                                    + mr.getReactorType()
                                    + " State: "
                                    + mr.getState()
                                    + " Event State: "
                                    + mr.getEventState()
                                    + " Position: x "
                                    + mr.getPosition().getX()
                                    + " y "
                                    + mr.getPosition().getY()
                                    + '.');
                }
                break;

            case "servercoupons":
            case "coupons":
                StringBuilder s = new StringBuilder("Currently active SERVER coupons: ");
                for (Integer i : Server.getActiveCoupons()) {
                    s.append(i).append(' ');
                }

                player.dropMessage(6, s.toString());
                break;

            case "playercoupons":
                StringBuilder st = new StringBuilder("Currently active PLAYER coupons: ");
                for (Integer i : player.getActiveCoupons()) {
                    st.append(i).append(' ');
                }

                player.dropMessage(6, st.toString());
                break;

            case "timer":
                TimerManager tMan = TimerManager.getInstance();
                player.dropMessage(
                        6,
                        "Total Task: "
                                + tMan.getTaskCount()
                                + " Current Task: "
                                + tMan.getQueuedTasks()
                                + " Active Task: "
                                + tMan.getActiveCount()
                                + " Completed Task: "
                                + tMan.getCompletedTaskCount());
                break;

            case "marriage":
                c.getChannelServer().debugMarriageStatus();
                break;
        }
    }
}
