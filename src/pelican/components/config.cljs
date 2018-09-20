(ns pelican.components.config
  (:require [pelican.db :refer [get-config
                                reset-config!
                                update-config!
                                APP-STATE]]
            [pelican.utils :as u]
            [pelican.analysis :as analysis]))

(defn update-config-on-change
  "Updates state variable for KEY on EVENT."
  [key event]
  (let [value (-> event
                  .-target
                  .-value)
        float-value (js/parseFloat value)]
    (if (not (js/isNaN float-value))
      (update-config! key float-value))))

(defn config-text-input-comp
  "Builds a text input component."
  [key]
  [:input {:type "text"
           :id key
           :style {:text-align "right"}
           :default-value (key (get-config))
           :on-change (partial update-config-on-change key)}])

(defn update-mortgage-payment-per-month
  [event]
  (let [config (get-config)
        home-price (:home-price config)
        downpayment (:downpayment config)
        loan-amount (- home-price downpayment)
        annual-interest-rate (:mortgage-interest-rate config)
        loan-duration-years (:mortgage-duration-years config)
        ppm (Math/round (analysis/compute-amortized-loan-payment-per-month loan-amount
                                                                           annual-interest-rate
                                                                           loan-duration-years))
        ppm-box (. js/document (getElementById "mortgage-ppm"))]
    (set! (.-value ppm-box) ppm)
    (update-config! :mortgage-ppm ppm)))

(defn config-comp
  "Builds configuration component."
  []
  (let [config (get-config)]
    [:div
     [:h3 "Configuration"
      [:input {:type "button"
               :style {:margin-left "10px"}
               :value "RESET"
               :on-click #(do (reset-config!)
                              (.reload (.-location js/window)))}]]

     [:div {:class "table config"}
      [:div.row.header.blue
       [:div.cell.title "Purchase information"]
       [:div.cell.title]]

      [:div.row
       [:div.cell "Home price"]
       [:div.cell.value [config-text-input-comp :home-price]]]

      [:div.row
       [:div.cell "Downpayment"]
       [:div.cell.value [config-text-input-comp :downpayment]]]

      [:div.row
       [:div.cell "Closing cost"]
       [:div.cell.value [config-text-input-comp :closing-cost]]]

      [:div.row.header.blue
       [:div.cell.title "Mortgage"]
       [:div.cell.title]]

      [:div.row
       [:div.cell "Mortgage interest rate"]
       [:div.cell.value [config-text-input-comp :mortgage-interest-rate]]]

      [:div.row
       [:div.cell "Private mortgage interest rate (PMI)"]
       [:div.cell.value [config-text-input-comp :private-mortgage-interest-rate]]]

      [:div.row
       [:div.cell "Mortgage duration (years)"]
       [:div.cell.value [config-text-input-comp :mortgage-duration-years]]]

      [:div.row
       [:div.cell "Mortgage payment per month"]
       [:div.cell.value [config-text-input-comp :mortgage-ppm]
        [:input {:type "button"
                 :style {:margin-left "10px" :display "inline"}
                 :value "COMPUTE"
                 :on-click update-mortgage-payment-per-month}]]]

      [:div.row
       [:div.cell "HOA fee per month"]
       [:div.cell.value [config-text-input-comp :hoa-ppm]]]

      [:div.row.header.blue
       [:div.cell.title "Taxes, insurance costs"]
       [:div.cell.title]]
      [:div.row
       [:div.cell "Home insurance cost rate (yearly)"]
       [:div.cell.value [config-text-input-comp :insurance-cost-rate]]]
      [:div.row
       [:div.cell "Income tax rate"]
       [:div.cell.value [config-text-input-comp :income-tax-rate]]]
      [:div.row
       [:div.cell "Property tax rate"]
       [:div.cell.value [config-text-input-comp :property-tax-rate]]]

      [:div.row.header.blue
       [:div.cell.title "Gain/loss parameters"]
       [:div.cell.title]]
      [:div.row
       [:div.cell "Home appreciation rate (yearly)"]
       [:div.cell.value [config-text-input-comp :home-appreciation-rate]]]
      [:div.row
       [:div.cell "Home sale cost rate"]
       [:div.cell.value [config-text-input-comp :home-sale-cost-rate]]]
      [:div.row
       [:div.cell "Maintenance cost rate (yearly)"]
       [:div.cell.value [config-text-input-comp :maintenance-cost-rate]]]

      [:div.row.header.blue
       [:div.cell.title "Alternative investment"]
       [:div.cell.title]]
      [:div.row
       [:div.cell "Monthly rent"]
       [:div.cell.value [config-text-input-comp :rent-ppm]]]
      [:div.row
       [:div.cell "Rent appreciation rate (yearly)"]
       [:div.cell.value [config-text-input-comp :rent-appreciation-rate]]]
      [:div.row
       [:div.cell "Alternative investment return rate"]
       [:div.cell.value [config-text-input-comp :alternate-investment-return-rate]]]]]))
