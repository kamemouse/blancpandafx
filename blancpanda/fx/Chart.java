package blancpanda.fx;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.List;

import javax.swing.Timer;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.time.Minute;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.ohlc.OHLCSeries;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;


public class Chart {
	private int max;
	private OHLCSeries series0;
	private CandleStick cs;
	private int period;
	private Date date;

	public JFreeChart createChart(){
		this.period = CandleStick.M1;
		this.cs = new CandleStick(CandleStick.USDJPY, period);
		this.max = 60;
		this.series0 = new OHLCSeries("s1");
		this.series0.setMaximumItemCount(max);

		loadCandleStick();

		OHLCSeriesCollection dataset = new OHLCSeriesCollection();
		dataset.addSeries(series0);

		DateAxis domain = new DateAxis("");	// Time
		NumberAxis range = new NumberAxis("");	// Price
		// 0を含まずに自動調整
		range.setAutoRangeIncludesZero(false);

		// ローソク足の色を変える
		CandlestickRenderer renderer = new CandlestickRenderer();
		// 陽線を白に
		renderer.setUpPaint(Color.WHITE);
		// 陰線を青に
		renderer.setDownPaint(Color.BLACK);
		// ローソクの枠を黒、1ピクセルで描画
		renderer.setUseOutlinePaint(true);
		renderer.setBaseOutlinePaint(Color.BLACK);
		renderer.setBaseOutlineStroke(new BasicStroke(1));

		XYPlot plot = new XYPlot(dataset, domain, range, renderer);
		JFreeChart chart = new JFreeChart(null, null, plot, false);
		return chart;
	}

	public class DataGenerator extends Timer implements ActionListener{
		private static final long serialVersionUID = 20090602L;

		public DataGenerator(int interval){
			super(interval, null);
			addActionListener(this);
		}

		public void actionPerformed(ActionEvent e) {
			addCandleStick();
		}
	}

	private void loadCandleStick(){
		CandleStickDao csDao = new CandleStickDao();
		List<CandleStick> list = csDao.getRecentList(period);
		int serice = list.size();
		for (int i = serice - 1; i >= 0; i--) { // 時間の降順で取得してくる
			cs = list.get(i);
			this.series0.add(new Minute(this.cs.getDate()), this.cs.getBid_open(), this.cs.getBid_high(), this.cs.getBid_low(), this.cs.getBid_close());
		}
//		addCandleStick();	// 最新の足を追加
	}

	public void addCandleStick() {
		this.date = this.cs.getCurrentRate();
		System.out.println(this.date);
		RegularTimePeriod prd = new Minute(this.date);
		int index = this.series0.indexOf(prd);
		System.out.println("index:" + index);
		if(index >= 0){
//			System.out.println("index >= 0");
			this.series0.remove(prd);
//			System.out.println("period:" + prd);
		}else{
			this.cs.clearRate();
		}
		this.series0.add(new Minute(this.cs.getDate()), this.cs.getBid_open(), this.cs.getBid_high(), this.cs.getBid_low(), this.cs.getBid_close());
		return;
	}
}
