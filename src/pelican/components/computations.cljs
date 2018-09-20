(ns pelican.components.computations
  (:require [pelican.db :as db]
            [pelican.utils :as u]))

(defn currency-cell [value]
  [:div.cell.currency (u/format-number-with-commas (Math/round value))])

(defn hide
  "Hides the given element."
  [e]
  (set! (.-display (.-style e)) "none"))

(defn show
  "Displays the given element."
  [e]
  (set! (.-display (.-style e)) "block"))

(defn handle-toggle-computations
  [e]
  (let [ctable (. js/document (getElementById "computations-table"))
        btn (. js/document (getElementById "toggle-computations"))
        current-value (.-value btn)]
    (if (= current-value "SHOW")
      (do (set! (.-value btn) "HIDE")
          (show ctable))
      (do (set! (.-value btn) "SHOW")
          (hide ctable)))))

(defn computations-comp
  "Builds computations table component."
  [computations]
  (let [config (db/get-config)]
    [:div [:h3 "Computations"
           [:input {:type "button"
                    :style {:margin-left "10px"}
                    :value "SHOW"
                    :id "toggle-computations"
                    :on-click handle-toggle-computations}]]
     [:div.table.computations {:id "computations-table"
                               :style {:display "none"}}
      [:div
       [:p.small "*PMI: Amount paid for private mortgage insurance when equity-gain < 20% of home value"]
       [:p.small "*tax-savings-ppm" ": Tax savings per month, capped at $750,000 principal"]
       [:p.small "*mortgage-ppm" ": Amount paid in mortgage per month = interest-ppm + principal-ppm"]
       ]
      [:div.row.header.blue
       [:div.cell.right "y"]
       [:div.cell.right "t"]
       [:div.cell.right "rent-ppm"]
       [:div.cell.right "*PMI"]
       [:div.cell.right "*mortgage-ppm"]
       [:div.cell.right "interest-ppm"]
       [:div.cell.right "principal-ppm"]
       [:div.cell.right "principal"]
       [:div.cell.right "equity-gain"]
       [:div.cell.right "*tax-savings-ppm"]
       [:div.cell.right "opportunity-cost"]
       [:div.cell.right "home-value"]
       [:div.cell.right "profit-from-sale"]]
      (doall
       (for [c computations]
         ^{:key (:t c)}
         [:div {:class "row regular"}
          [:div.cell.right (:y c)]
          [:div.cell.right (:t c)]
          [currency-cell (:rent-ppm c)]
          [currency-cell (:pmi-ppm c)]
          [currency-cell (:mortgage-ppm c)]
          [currency-cell (:interest-ppm c)]
          [currency-cell (:principal-ppm c)]
          [currency-cell (:principal c)]
          [currency-cell (:equity-gain c)]
          [currency-cell (:savings-tax c)]
          [currency-cell (:opportunity-cost c)]
          [currency-cell (:home-value c)]
          [currency-cell (:profit-from-sale c)]]))]]))
