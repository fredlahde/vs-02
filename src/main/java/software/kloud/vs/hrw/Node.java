package software.kloud.vs.hrw;

import java.util.ArrayList;
import java.util.List;

public class Node<T extends Workload> {
    private final List<T> workloads;
    private static final Object lock = 0;
    private final String id;

    public Node(String id) {
        this.id = id;
        workloads = new ArrayList<>();
    }

    public void add(T workload) {
        synchronized (lock) {
            workloads.add(workload);
        }
    }

    public void remove(T workload) {
        synchronized (lock) {
            workloads.removeIf(w -> w.getId().equals(workload.getId()));
        }
    }

    public int count() {
        return workloads.size();
    }

    public String getId() {
        return id;
    }
}
