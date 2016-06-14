package cn.lambdalib.core;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple profiler to accumulate time in different sections.
 */
public class Profiler {

    private class SectionData {
        String section;
        float timeTotal;
        int times;

        long beginTime;
        boolean calculating;
    }

    private Map<String, SectionData> sections = new HashMap<>();

    public void begin(String section) {
        if (!sections.containsKey(section)) {
            SectionData data = new SectionData();
            data.section = section;

            sections.put(section, data);
        }

        SectionData data = sections.get(section);
        Preconditions.checkState(!data.calculating, "Section " + section + " already started");

        data.calculating = true;
        data.beginTime = time();
        data.times += 1;
    }

    public void end(String section) {
        SectionData data = Preconditions.checkNotNull(sections.get(section), "Section " + section + " not started");
        Preconditions.checkState(data.calculating, "Section " + section + " not started");

        long delta = time() - data.beginTime;
        data.timeTotal += delta / 1000.0f;

        data.calculating = false;
    }

    private long time() { return System.currentTimeMillis(); }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Profiling result: \n");

        List<SectionData> sorted = new ArrayList<>(sections.values());
        sorted.sort((lhs, rhs) -> Float.compare(lhs.timeTotal, rhs.timeTotal));

        float total = 0.0f;

        for (SectionData data : sorted) {
            sb.append(String.format("[%s]: %.2fs (%d times) ", data.section, data.timeTotal, data.times));
            if (data.calculating) {
                sb.append("Calculating\n");
            } else {
                sb.append("\n");
            }

            total += data.timeTotal;
        }

        sb.append(String.format("Time total: %.2fs. \n", total));

        return sb.toString();
    }

}
