import java.util.Comparator;

public class PriComparator implements Comparator<Element> {
    @Override
    public int compare(Element o1, Element o2) {
        if (o1.getPriority() < o2.getPriority()) {
            return -1;
        } else if (o2.getPriority() < o1.getPriority()) {
            return 1;
        }
        return 0;
    }
}
