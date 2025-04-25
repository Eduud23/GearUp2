package com.example.gearup;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class LineChartAdapter extends RecyclerView.Adapter<LineChartAdapter.ChartViewHolder> {

    private final List<ForecastModel> forecastList;

    public LineChartAdapter(List<ForecastModel> forecastList) {
        this.forecastList = forecastList;
    }

    @NonNull
    @Override
    public ChartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chart, parent, false);
        return new ChartViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ChartViewHolder holder, int position) {
        ForecastModel data = forecastList.get(position);
        holder.productTitle.setText(data.getProductLine());

        // Prepare entries for the chart (Sales Forecast)
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < data.getXValues().size(); i++) {
            entries.add(new Entry(data.getXValues().get(i), data.getYValues().get(i)));
        }

        // Create dataset for sales forecast
        LineDataSet dataSet = new LineDataSet(entries, "Sales Forecast");
        dataSet.setColor(0xFF1E88E5);  // Line color
        dataSet.setCircleColor(0xFF1E88E5);  // Circle color
        dataSet.setLineWidth(2f);
        dataSet.setDrawValues(false);  // Hide values on the graph

        // Apply the data to the chart
        LineData lineData = new LineData(dataSet);
        holder.chart.setData(lineData);
        holder.chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        holder.chart.getXAxis().setDrawLabels(false);  // Hide X-axis labels
        holder.chart.getAxisRight().setEnabled(false);  // Disable right Y-axis
        holder.chart.getDescription().setEnabled(false);  // Disable chart description
        holder.chart.invalidate();  // Refresh chart

        // Round forecasted quantity to nearest whole number
        int roundedForecastQuantity = Math.round(data.getForecastQuantity());

        // Show forecast information (both sales and quantity)
        String forecastText = "\uD83D\uDCC5 Forecast Date: " + data.getForecastDate() +
                "\nPredicted Sales: ₱" + String.format("%.2f", data.getForecastSales()) +
                "\nPredicted Quantity: " + roundedForecastQuantity + " units" +
                "\n\uD83D\uDCC8 Trend: " + data.getTrendDirection();

        holder.forecastInfo.setText(forecastText);

        // Handle click to go to DetailActivity
        holder.itemView.setOnClickListener(view -> {
            Context context = view.getContext();
            Intent intent = new Intent(context, ForecastDetail.class);
            intent.putExtra("productTitle", data.getProductLine());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return forecastList.size();
    }

    // ViewHolder class to hold the chart and forecast info
    public static class ChartViewHolder extends RecyclerView.ViewHolder {
        TextView productTitle;
        LineChart chart;
        TextView forecastInfo;

        public ChartViewHolder(@NonNull View itemView) {
            super(itemView);
            productTitle = itemView.findViewById(R.id.productTitle);
            chart = itemView.findViewById(R.id.lineChart);
            forecastInfo = itemView.findViewById(R.id.forecastInfo);
        }
    }
}
