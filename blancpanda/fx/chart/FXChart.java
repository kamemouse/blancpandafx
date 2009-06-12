package blancpanda.fx.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.List;

import javax.swing.Timer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
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
import blancpanda.fx.timeperiod.Minute30;
import blancpanda.fx.timeperiod.Minute5;

public class FXChart {

	private static Display display;
	private static ChartComposite chartComposite;

	private OHLCSeries candle;
	private CandleStick cs;
	private RegularTimePeriod pre_period;
	private int period;
	private int currency_pair;
	private static Combo cmb_period;
	private static Combo cmb_currency_pair;

	class ChartChanger implements SelectionListener {
		
		public void widgetDefaultSelected(SelectionEvent selectionevent) {
			updateChart();
		}

		public void widgetSelected(SelectionEvent selectionevent) {
			updateChart();
		}
		
		private void updateChart() {
			boolean changed = false;
			if(period != cmb_period.getSelectionIndex()){
				// ピリオドが変わった
				period = cmb_period.getSelectionIndex();
				changed = true;
			}
			if(currency_pair != cmb_currency_pair.getSelectionIndex()){
				// 通貨ペアが変わった
				currency_pair = cmb_currency_pair.getSelectionIndex();
				changed = true;
			}
			if(changed){
				// ピリオドか通貨ペアが変わったら
				// CandleStickを書き換える
				cs = new CandleStick(currency_pair, period);
				// ロウソク足を全部消す
				candle.clear();
				// DBから読み込み直す
				loadCandleStick();
				// チャートの書き直し
				chartComposite.forceRedraw();
				changed = false;
			}
		}
	}
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
					updateCandleStick();

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
		this.currency_pair = currency_pair;
		// pre_periodの初期化
		pre_period = getRegularTimePeriod(new Date());
	}

	private RegularTimePeriod getRegularTimePeriod(Date date) {
		RegularTimePeriod ret = null;
		switch (period) {
		case CandleStick.M1:
			ret = new Minute(date);
			break;
		case CandleStick.M5:
			ret = new Minute5(date);
			break;
		case CandleStick.M30:
			ret = new Minute30(date);
			break;
		default:
			System.out.println("ピリオドが不正です。");
			break;
		}
		return ret;
	}

	public JFreeChart createChart() {
		loadCandleStick();

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
/*
		CandleStickDao csDao = new CandleStickDao();
		List<CandleStick> list = csDao.getRecentList(period);
		int serice = list.size();
		for (int i = serice - 1; i >= 0; i--) { // 時間の降順で取得してくる
			cs = list.get(i);
			// DBに重複データができてしまっている可能性がある
			RegularTimePeriod prd = getRegularTimePeriod(cs.getDate());
			int index = candle.indexOf(prd);
//			System.out.println("index:" + index);
			if (index >= 0) {
				candle.remove(prd);
			}
			candle.add(prd, cs.getBid_open(), cs.getBid_high(),
					cs.getBid_low(), cs.getBid_close());
		}
*/
	}

	@SuppressWarnings("unchecked")
	private void updateCandleStick() {
		Date date = cs.getCurrentRate();
//		System.out.println(date);
		// なぜか取得日時が逆戻りするので、戻った場合には何もしない。
		RegularTimePeriod prd = getRegularTimePeriod(date);
		if (prd.compareTo(pre_period) >= 0) {
			int index = candle.indexOf(prd);
//			System.out.println("index:" + index);
			if (index >= 0) {
				candle.remove(prd);
			} else {
				cs.clearRate();
			}
			candle.add(prd, cs.getBid_open(), cs.getBid_high(),
					cs.getBid_low(), cs.getBid_close());
			pre_period = prd;
		} else {
//			System.out.println("逆戻り");
		}
	}

	public static void main(String args[]) {
		display = new Display();
		Shell shell = new Shell(display);
		shell.setSize(600, 480);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 4;
		layout.marginWidth = 4;
		shell.setLayout(layout);
		shell.setText("リアルタイム為替チャート");
		
		Composite toolbar = new Composite(shell, SWT.NONE);
		GridLayout lo_toolbar = new GridLayout();
		lo_toolbar.numColumns = 2;
		toolbar.setLayout(lo_toolbar);
		GridData ld_toolbar = new GridData(GridData.FILL_HORIZONTAL);
		toolbar.setLayoutData(ld_toolbar);
		
		FXChart fxchart = new FXChart(CandleStick.M5, CandleStick.USDJPY, 60);
		JFreeChart chart = fxchart.createChart();

		// 通貨ベア選択コンボの追加
		cmb_currency_pair = new Combo(toolbar,SWT.DROP_DOWN|SWT.BORDER|SWT.READ_ONLY);
		cmb_currency_pair.addSelectionListener(fxchart.new ChartChanger());
		cmb_currency_pair.add("USD/CAD");
		cmb_currency_pair.add("EUR/JPY");
		cmb_currency_pair.add("NZD/JPY");
		cmb_currency_pair.add("GBP/CHF");
		cmb_currency_pair.add("USD/CHF");
		cmb_currency_pair.add("ZAR/JPY");
		cmb_currency_pair.add("NZD/USD");
		cmb_currency_pair.add("CAD/JPY");
		cmb_currency_pair.add("EUR/GBP");
		cmb_currency_pair.add("USD/JPY");	// 米ドル円
		cmb_currency_pair.add("CHF/JPY");
		cmb_currency_pair.add("GBP/JPY"); 	// 英ポンド円
		cmb_currency_pair.add("GBP/USD");
		cmb_currency_pair.add("AUD/JPY");
		cmb_currency_pair.add("EUR/USD");
		cmb_currency_pair.add("AUD/USD");
		cmb_currency_pair.select(CandleStick.USDJPY);
//		cmb_currency_pair.setEnabled(false);	// とりあえず、米ドル円のみ

		// ピリオド選択コンボの追加
		cmb_period = new Combo(toolbar,SWT.DROP_DOWN|SWT.BORDER|SWT.READ_ONLY);
		cmb_period.addSelectionListener(fxchart.new ChartChanger());
		cmb_period.add("M1");
		cmb_period.add("M5");
		cmb_period.add("M30");
		cmb_period.select(CandleStick.M5);

		chartComposite = new ChartComposite(shell, SWT.NONE, chart, true);
		chartComposite.setDisplayToolTips(true);
		chartComposite.setHorizontalAxisTrace(false);
		chartComposite.setVerticalAxisTrace(false);
		GridData ld_chart = new GridData(GridData.FILL_BOTH);
		chartComposite.setLayoutData(ld_chart);
		
		shell.open();

		Timer timer = fxchart.new DataGenerator(1000);
		timer.start();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		
		timer.stop();
		display.dispose();
	}
}
