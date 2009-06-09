package blancpanda.fx.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.List;

import javax.swing.Timer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.time.Minute;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.ohlc.OHLCSeries;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;
import org.jfree.experimental.chart.swt.ChartComposite;

import blancpanda.fx.CandleStick;
import blancpanda.fx.CandleStickDao;

public class FXChart {

	private static Display display;
	private static ChartComposite chartComposite;

	private OHLCSeries candle;
	private CandleStick cs;
	private Date pre_date;
	private int period;

	class DataGenerator extends Timer implements ActionListener {
		/**
		 *
		 */
		private static final long serialVersionUID = 200906080L;

		DataGenerator(int i) {
			super(i, null);
			addActionListener(this);
		}

		public void actionPerformed(ActionEvent actionevent) {
			// [SILENT] for SWT UI thread issue, use Display.asyncExec()
			display.asyncExec(new Runnable() {
				public void run() {
					addCandleStick();

					// [SILENT] for chart redraw, use
					// ChartComposite.forceRedraw();
					// if not use this, then chart will not redraw.
					// I don't know why.
					chartComposite.forceRedraw();
				}
			});
		}
	}

	/*
	 * period = CandleStick.M1 currency_pair = CandleStick.USDJPY max = 60
	 */
	public FXChart(int period, int currency_pair, int max) {
		candle = new OHLCSeries("s1");
		candle.setMaximumItemCount(max);
		cs = new CandleStick(currency_pair, period);
		this.period = period;
		pre_date = new Date();
	}

	public JFreeChart createChart() {
		//loadCandleStick();

		OHLCSeriesCollection dataset = new OHLCSeriesCollection();
		dataset.addSeries(candle);

		DateAxis domain = new DateAxis(""); // Time
		NumberAxis range = new NumberAxis(""); // Price
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
		JFreeChart jfreechart = new JFreeChart(null, null, plot, false);
		return jfreechart;
	}

	private void loadCandleStick() {
		CandleStickDao csDao = new CandleStickDao();
		List<CandleStick> list = csDao.getRecentList(period);
		int serice = list.size();
		for (int i = serice - 1; i >= 0; i--) { // 時間の降順で取得してくる
			cs = list.get(i);
			candle.add(new Minute(cs.getDate()), cs.getBid_open(), cs
					.getBid_high(), cs.getBid_low(), cs.getBid_close());
		}
	}

	private void addCandleStick() {
		Date date = cs.getCurrentRate();
		System.out.println(date);
		if(date.compareTo(pre_date) >= 0){
			// なぜか取得日時が逆戻りするので、戻った場合には何もしない。
			RegularTimePeriod prd = new Minute(date);
			int index = candle.indexOf(prd);
			System.out.println("index:" + index);
			if (index >= 0) {
				candle.remove(prd);
			} else {
				cs.clearRate();
			}
			candle.add(prd, cs.getBid_open(),
					cs.getBid_high(), cs.getBid_low(), cs.getBid_close());
			pre_date = date;
		}else{
			System.out.println("逆戻り");
		}
	}

	public static void main(String args[]) {
		display = new Display();
		Shell shell = new Shell(display);
		shell.setSize(600, 480);
		shell.setLayout(new FillLayout());
		shell.setText("リアルタイム為替チャート");

		FXChart fxchart = new FXChart(CandleStick.M1, CandleStick.USDJPY, 60);
		JFreeChart chart = fxchart.createChart();

		chartComposite = new ChartComposite(shell, SWT.NONE, chart, true);
		chartComposite.setDisplayToolTips(true);
		chartComposite.setHorizontalAxisTrace(false);
		chartComposite.setVerticalAxisTrace(false);
		shell.open();

		Timer timer = fxchart.new DataGenerator(1000);
		timer.start();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}
}
