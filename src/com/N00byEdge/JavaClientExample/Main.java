package com.N00byEdge.JavaClientExample;

import com.N00byEdge.JavaBWAPIBackend.Client;

import java.util.TreeSet;

public class Main {
    private static int distance(int x1, int y1, int x2, int y2) {
        return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
    }

    public static void main(String[] args) throws InterruptedException {
        Client client = null;
        while(client == null) {
            try {
                client = new Client();
            }
            catch (Throwable t) {
                System.out.println("Cannot connect:");
                t.printStackTrace();
                Thread.sleep(1000);
            }
        }

        TreeSet<Integer> visibleUnits = new TreeSet<>();

        Client.EventHandler handler = (event) -> {
            switch (event.type()) {
                case 10: // UnitShow
                    visibleUnits.add(event.v1());
                    break;

                case 11: // UnitHide
                    visibleUnits.add(event.v1());
                    break;
            }
        };

        try {
            while(!client.data().isInGame()) client.update(handler);
            System.out.println("Game started!");
            client.data().addCommand(client.data().new Command(5, client.data().addString("Hello world!"), 0));

            Client.GameData.PlayerData self = client.data().getPlayer(client.data().self());
            int startX = self.startLocationX();
            int startY = self.startLocationY();

            Client.GameData.UnitData closestMineral = null;
            int minDist = 0;
            for(int i: visibleUnits) {
                Client.GameData.UnitData unit = client.data().unit(i);
                if(unit.resources() == 1500) {
                    int dist = distance(unit.positionX(), unit.positionY(), startX, startY);
                    if(dist < minDist || closestMineral == null) {
                        minDist = dist;
                        closestMineral = unit;
                    }
                }
            }

            if(closestMineral == null) return;

            while(client.data().isInGame()) {
                client.update(handler);
                for(int i: visibleUnits) {
                    Client.GameData.UnitData unit = client.data().unit(i);
                    if(unit.player() == self.id() && unit.isIdle() && unit.type() == 41) { // 41 = drone
                        client.data().addUnitCommand(client.data().new UnitCommand(15, unit.id(), closestMineral.id(), 0, 0, 0));
                    }
                    else if(unit.player() == self.id() && unit.type() == 35) { // 35 = larva
                        client.data().addUnitCommand(client.data().new UnitCommand(5, unit.id(), 0, 0, 0, 41));
                    }
                }
            }
        }
        catch (Throwable t) {

        }
    }
}
