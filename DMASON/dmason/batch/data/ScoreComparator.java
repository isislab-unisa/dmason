package dmason.batch.data;

import java.util.Comparator;


public class ScoreComparator implements Comparator<EntryWorkerScore<Integer, String>>{

	@Override
	public int compare(EntryWorkerScore<Integer, String> o1,
			EntryWorkerScore<Integer, String> o2) {
		return (o1.getScore() > o2.getScore() ? -1 : (o1.getScore() == o2.getScore() ? 0 : 1));
	}
 


}
