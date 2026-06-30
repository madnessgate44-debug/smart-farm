package com.ahmedpasha.smartfarm.ui.screens

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.ahmedpasha.smartfarm.ui.viewmodel.FarmViewModel
import org.json.JSONArray

@Composable
fun ChartsScreen(viewModel: FarmViewModel) {
    val crops by viewModel.crops.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text("📊 الأداء والإنتاجية", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(16.dp))

        if (crops.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📭", style = MaterialTheme.typography.headlineLarge)
                    Text("لا توجد بيانات محاصيل بعد", style = MaterialTheme.typography.titleMedium)
                    Text("يرجى إضافة بيانات المحاصيل في قسم الجداول أولاً", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth().weight(1f).padding(8.dp), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                D3ChartView(cropsDataJson = buildCropsJson(crops))
            }
        }
    }
}

private fun buildCropsJson(crops: List<com.ahmedpasha.smartfarm.data.models.Crop>): String {
    val jsonArray = JSONArray()
    crops.forEach { crop ->
        val obj = org.json.JSONObject()
        obj.put("crop", crop.crop)
        obj.put("expected", crop.expectedProduction)
        obj.put("actual", crop.actualProduction)
        jsonArray.put(obj)
    }
    return jsonArray.toString()
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun D3ChartView(cropsDataJson: String) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                settings.javaScriptEnabled = true
                webViewClient = WebViewClient()
                loadDataWithBaseURL(null, getD3ChartHtml(cropsDataJson), "text/html", "UTF-8", null)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

private fun getD3ChartHtml(dataJson: String): String = """
<!DOCTYPE html>
<html dir="rtl" lang="ar">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <script src="https://cdn.jsdelivr.net/npm/d3@7"></script>
    <style>
        body { margin: 0; padding: 20px; background: #1E293B; font-family: sans-serif; }
        .title { text-align: center; color: #F8FAFC; font-size: 18px; margin-bottom: 10px; }
        .legend { display: flex; justify-content: center; gap: 20px; margin-bottom: 10px; }
        .legend-item { display: flex; align-items: center; gap: 5px; color: #F8FAFC; }
        .legend-color { width: 15px; height: 15px; border-radius: 3px; }
        .tooltip { position: absolute; background: rgba(0,0,0,0.9); color: white; padding: 8px 12px; border-radius: 6px; font-size: 12px; pointer-events: none; opacity: 0; }
    </style>
</head>
<body>
    <div class="title">مقارنة الإنتاج المتوقع مقابل الفعلي</div>
    <div class="legend">
        <div class="legend-item"><div class="legend-color" style="background:#F59E0B;"></div>الإنتاج المتوقع</div>
        <div class="legend-item"><div class="legend-color" style="background:#10B981;"></div>الإنتاج الفعلي</div>
    </div>
    <div id="chart"></div>
    <div class="tooltip" id="tooltip"></div>
    <script>
        const data = $dataJson;
        const margin = {top: 20, right: 40, bottom: 60, left: 60};
        const width = 600 - margin.left - margin.right;
        const height = 350 - margin.top - margin.bottom;
        const svg = d3.select("#chart").append("svg").attr("width", width + margin.left + margin.right).attr("height", height + margin.top + margin.bottom).append("g").attr("transform", "translate(" + margin.left + "," + margin.top + ")");
        const crops = data.map(d => d.crop);
        const x0 = d3.scaleBand().domain(crops).range([width, 0]).padding(0.2);
        const x1 = d3.scaleBand().domain(["expected", "actual"]).range([0, x0.bandwidth()]).padding(0.05);
        const y = d3.scaleLinear().domain([0, d3.max(data, d => Math.max(d.expected, d.actual)) * 1.1]).range([height, 0]);
        const tooltip = d3.select("#tooltip");
        svg.append("g").attr("transform", "translate(0," + height + ")").call(d3.axisBottom(x0)).selectAll("text").style("fill", "#F8FAFC");
        svg.append("g").attr("transform", "translate(" + width + ",0)").call(d3.axisRight(y)).selectAll("text").style("fill", "#F8FAFC");
        const groups = svg.selectAll("g.group").data(data).enter().append("g").attr("transform", d => "translate(" + x0(d.crop) + ",0)");
        groups.append("rect").attr("x", x1("expected")).attr("y", d => y(d.expected)).attr("width", x1.bandwidth()).attr("height", d => height - y(d.expected)).attr("fill", "#F59E0B").attr("rx", 4).on("mouseover", function(event, d) { tooltip.style("opacity", 1).html(d.crop + " (المتوقع) " + d.expected + " طن").style("left", (event.pageX - 50) + "px").style("top", (event.pageY - 40) + "px"); }).on("mouseout", () => tooltip.style("opacity", 0));
        groups.append("rect").attr("x", x1("actual")).attr("y", d => y(d.actual)).attr("width", x1.bandwidth()).attr("height", d => height - y(d.actual)).attr("fill", "#10B981").attr("rx", 4).on("mouseover", function(event, d) { tooltip.style("opacity", 1).html(d.crop + " (الفعلي) " + d.actual + " طن").style("left", (event.pageX - 50) + "px").style("top", (event.pageY - 40) + "px"); }).on("mouseout", () => tooltip.style("opacity", 0));
        svg.append("g").attr("class", "grid").call(d3.axisRight(y).tickSize(-width).tickFormat("")).selectAll("line").style("stroke", "#334155").style("stroke-dasharray", "3,3");
    </script>
</body>
</html>
""".trimIndent()