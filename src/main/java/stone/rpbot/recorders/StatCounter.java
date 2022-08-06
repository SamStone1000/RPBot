package stone.rpbot.recorders;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalUnit;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Stack;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.TimeUtil;
import stone.rpbot.util.MutableInteger;

public class StatCounter {

	private boolean shouldAffect;
	private NavigableMap<OffsetDateTime, MutableInteger> count; // messages sent a month
	private int currentCount;
	private OffsetDateTime creationTime;
	// private Pattern pattern = Pattern.compile(")

	public StatCounter(boolean shouldAffect, OffsetDateTime creation) {
		this.shouldAffect = shouldAffect;
		this.count = new TreeMap<OffsetDateTime, MutableInteger>();
		OffsetDateTime now = OffsetDateTime.now();
		while (creation.isBefore(now))
		{
			count.put(creation, new MutableInteger(0));
			creation = creation.plusDays(1);
		}
	}

	public void accept(Message message) {
		OffsetDateTime messageTime = TimeUtil.getTimeCreated(message.getIdLong());
		count.floorEntry(messageTime).getValue().increment();
	}

	@Override
	public String toString() {
		String output = "";
		for (Entry<OffsetDateTime, MutableInteger> entry : count.entrySet())
		{
			output += entry.toString();
			output += "\n";
		}
		return output;
	}

	public byte[] outputGraph() {
		Logger logger = LoggerFactory.getLogger("Graphing");
		DefaultXYDataset data = new DefaultXYDataset();
		double[][] dataArray = new double[2][count.size()];
		int i = 0;
		for (Entry<OffsetDateTime, MutableInteger> entry : count.entrySet())
		{
			dataArray[0][i] = entry.getKey().toEpochSecond();
			dataArray[1][i] = entry.getValue().doubleValue();
			i++;
			// logger.debug(Integer.toString(i));
		}
		data.addSeries("messagesOverTime", dataArray);
		JFreeChart chart = ChartFactory
				.createXYLineChart("Janky Hack", "Seconds since Epoch", "Messages Sent that Month", data);
		chart.removeLegend();
		BufferedImage image = chart.createBufferedImage(1024, 512);

		ByteArrayOutputStream BAOS = new ByteArrayOutputStream();
		try
		{
			ImageIO.write(image, "PNG", BAOS);
		} catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return BAOS.toByteArray();
	}
}
