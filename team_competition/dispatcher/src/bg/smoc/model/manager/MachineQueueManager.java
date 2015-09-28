package bg.smoc.model.manager;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import bg.smoc.agent.GraderAgent;
import bg.smoc.model.Contest;

public class MachineQueueManager {
    protected List<GraderAgent> idleMachineQueue = new LinkedList<GraderAgent>();
    protected List<GraderAgent> busyMachineQueue = new LinkedList<GraderAgent>();

    // Collections.synchronizedList(

    public List<GraderAgent> getIdleMachineQueue() {
        return Collections.unmodifiableList(idleMachineQueue);
    }

    public List<GraderAgent> getBusyMachineQueue() {
        return Collections.unmodifiableList(busyMachineQueue);
    }

    public boolean releaseMachineFromBusyMode(GraderAgent gm) {
        if (gm == null) {
            return false;
        }
        synchronized (this) {
            boolean result = busyMachineQueue.remove(gm);
            idleMachineQueue.add(gm);
            return result;
        }
    }

    public void registerMachine(GraderAgent gm) {
        synchronized (this) {
            idleMachineQueue.add(gm);
        }
    }

    public String removeMachine(GraderAgent gm) {
        String result = "neither";
        synchronized (this) {
            boolean idle = idleMachineQueue.remove(gm);
            if (idle) {
                result = "idle";
            }
            boolean busy = busyMachineQueue.remove(gm);
            if (busy) {
                result = "busy";
            }
        }
        return result;
    }

    public GraderAgent moveMachineToBusy() {
        synchronized (this) {
            if (idleMachineQueue.isEmpty())
                return null;
            GraderAgent gm = idleMachineQueue.remove(0);
            busyMachineQueue.add(gm);
            return gm;
        }
    }

    public int markAllGradersToUpdate(Contest contest) {
        int machinesToBeUpdated = 0;
        synchronized (this) {
            for (GraderAgent machine : idleMachineQueue) {
                machine.setNeedsUpdate(contest.getId());
                machinesToBeUpdated++;
            }
            for (GraderAgent machine : busyMachineQueue) {
                machine.setNeedsUpdate(contest.getId());
                machinesToBeUpdated++;
            }
        }
        return machinesToBeUpdated;
    }
}
