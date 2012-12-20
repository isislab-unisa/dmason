package dmason.batch.data;




public class EntryWorkerScore<Integer,String>
{
	private int score;
	private String topic;
	public EntryWorkerScore(int score, String topic) {
		super();
		this.score = score;
		this.topic = topic;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	@Override
	public java.lang.String toString() {
		return "[score=" + score + ", topic=" + topic + "]";
	}
	
	
	
	

}
