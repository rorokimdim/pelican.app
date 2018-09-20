(ns pelican.components.chart
  (:require cljsjs.plotly))

(defn draw-chart
  [computations]
  (let [yearly-computations (map last (partition-all 12 computations))]
    (js/Plotly.newPlot "chart"
                       (clj->js [{:x (map :y yearly-computations)
                                  :y (map :home-value yearly-computations)
                                  :name "Home value"
                                  :mode "lines+markers"},
                                 {:x (map :y yearly-computations)
                                  :y (map :sale-value yearly-computations)
                                  :name "Sale value"
                                  :mode "lines+markers"}
                                 {:x (map :y yearly-computations)
                                  :y (map :profit-from-sale yearly-computations)
                                  :name "Profit from sale"
                                  :yaxis "y2"
                                  :type "bar"
                                  :marker {:opacity 0.2}}
                                 {:x (map :y yearly-computations)
                                  :y (map :opportunity-cost yearly-computations)
                                  :name "Opportunity cost"
                                  :mode "lines+markers"}
                                 ])
                       (clj->js {:margin {:t 0}
                                 :title ""
                                 :autosize true
                                 :xaxis {:title "years" :hoverformat ",.0f"}
                                 :yaxis {:hoverformat ",.0f"}
                                 :yaxis2 {:overlaying "y"
                                          :side "right"
                                          :tickfont {:color "#1fb45c"}}}))))

(defn chart-comp
  [computations]
  (draw-chart computations))
