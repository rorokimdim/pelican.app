(ns pelican.components.summary
  (:require [pelican.db :as db]
            [pelican.utils :as u]))

(defn summary-comp
  "Builds summary component."
  [computations]
  (let [config (db/get-config)
        break-even-point (some #(when (not (neg? (:profit-from-sale %))) %) computations)
        home-price-f (u/format-number-with-commas (:home-price config))
        downpayment-f (u/format-number-with-commas (:downpayment config))
        mortgage-duration-years-f (:mortgage-duration-years config)
        mortgage-amount-f (u/format-number-with-commas (- (:home-price config) (:downpayment config)))
        mortgage-interest-rate-f (u/format-number-as-pct (:mortgage-interest-rate config))
        hoa-fee-f (u/format-number-with-commas (:hoa-ppm config))
        home-appreciation-rate-f (u/format-number-as-pct (:home-appreciation-rate config))
        alternate-investment-return-rate-f (u/format-number-as-pct (:alternate-investment-return-rate config))
        home-sale-cost-rate-f (u/format-number-as-pct (:home-sale-cost-rate config))
        rent-ppm-f (u/format-number-with-commas (:rent-ppm config))
        rent-appreciation-rate-f (u/format-number-as-pct (:rent-appreciation-rate config))]
    [:div
     [:h3 "The grand plan"]
     [:p
      "You plan to purchase a home for $" home-price-f " with downpayment of $" downpayment-f "."]

     [:p
      "You are taking a " mortgage-duration-years-f "-year mortgage of $" mortgage-amount-f
      " at " mortgage-interest-rate-f " interest. HOA fee is $" hoa-fee-f " per month."]

     [:p
      "You expect your home to appreciate at "
      home-appreciation-rate-f
      " every year. Your sale cost is expected to be "
      home-sale-cost-rate-f
      " of home value."]

     [:p
      "If you instead decide to rent, you expect to spend $"
      rent-ppm-f
      " in rent per month (with "
      rent-appreciation-rate-f
      " appreciation every year). You will put your downpayment of $"
      downpayment-f
      " in alternative investments at "
      alternate-investment-return-rate-f
      " interest."]

     (if break-even-point
       [:div [:h1 "Buying will be cheaper than renting in "
              (:y break-even-point)
              " years."]]
       [:div [:h1 "Buying will never be cheaper than renting."]])]))
