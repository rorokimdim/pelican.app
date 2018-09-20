(ns pelican.analysis)

(def MAX-DEDUCTIBLE-PRINCIPAL 750000)

(defn compute-amortized-loan-payment-per-month
  "Computes amortized payment per month.

  See derivation in https://en.wikipedia.org/wiki/Amortization_calculator."
  [loan-amount annual-interest-rate loan-duration-years]
  (let [r (inc (/ annual-interest-rate 12))
        n (* loan-duration-years 12)
        r_pow_n (Math/pow r n)
        payment-per-month (-> loan-amount
                              (* r_pow_n)
                              (* (dec r))
                              (/ (dec r_pow_n)))]
    payment-per-month))

(defn compute-mortgage-balance
  "Computes mortgage balance over time.

  options:
      :downpayment downpayment made (defaults to 20% of purchase-price)
      :duration-years duration of mortgage in years (defaults to 30 years)
      :interest-rate annual rate of interest (defaults to 0.04)
      :ppm payments to make every month
           (defaults to minimum amount to pay off mortgage in 30 years)"
  ([purchase-price] (compute-mortgage-balance purchase-price {}))
  ([purchase-price options] (compute-mortgage-balance
                             purchase-price
                             {:n 0
                              :cumulative-interest-paid 0
                              :principal-remaining nil}
                             options))
  ([purchase-price
    {:keys [n cumulative-interest-paid principal-remaining]}
    {:keys [downpayment
            duration-years
            interest-rate
            ppm]
     :or {downpayment (* 0.20 purchase-price)
          duration-years 30
          interest-rate 0.04}
     :as options}]
   (let [principal-remaining (or principal-remaining
                                 (- purchase-price downpayment))
         interest-ppm (-> principal-remaining
                          (* interest-rate)
                          (/ 12))
         deductible-interest-ppm (-> (min principal-remaining MAX-DEDUCTIBLE-PRINCIPAL)
                                     (* interest-rate)
                                     (/ 12))
         ppm (min (or ppm
                      (compute-amortized-loan-payment-per-month
                       (- purchase-price downpayment)
                       interest-rate
                       duration-years))
                  principal-remaining)
         principal-ppm (- ppm interest-ppm)
         cumulative-interest-paid (+ cumulative-interest-paid interest-ppm)]
     (when (and (pos? principal-remaining)
                (< n (* 12 duration-years)))
       (lazy-seq (cons {:t n
                        :principal principal-remaining
                        :ppm ppm
                        :interest-ppm interest-ppm
                        :deductible-interest-ppm deductible-interest-ppm
                        :principal-ppm principal-ppm
                        :cumulative-interest-paid cumulative-interest-paid}
                       (compute-mortgage-balance purchase-price
                                                 {:n (inc n)
                                                  :cumulative-interest-paid cumulative-interest-paid
                                                  :principal-remaining (- principal-remaining principal-ppm)}
                                                 (assoc options :ppm ppm))))))))

(defn buy-vs-rent
  "Computes buy-vs-rent analysis over time.

  options:
      :downpayment downpayment made for home (defaults to 20% of home-price)
      :closing-cost closing cost when buying home (defaults to 4% of home-price)
      :mortgage-duration-years duration of mortgage in years (defaults to 30 years)
      :mortgage-ppm payment made per month on mortgage
                                (defaults to minimum amount to pay off mortgage in mortgage-duration-years)
      :mortgage-interest-rate annual interest rate on mortgage (defaults to 0.04)
      :private-mortgage-interest-rate private mortgage interest rate (defaults to 0)
                                      (applied only if equity < 20% purchase price)
      :rent-ppm expected monthly rent if renting instead of buying a home
                    (defaults to 2500)
      :hoa-ppm Montly HOA cost for home if any (defaults to 0)
      :home-appreciation-rate rate in percentage at which home value appreciates annually
                              (defaults to 0)
      :rent-appreciation-rate rate in percentage at which rent increases annually
                              (defaults to 0)
      :property-tax-rate annual property tax in percentage of home purchase value
                         (defaults to 0.01)
      :maintenance-cost-rate maintenance cost in percentage of home purchase value
                             (defaults to 0.01)
      :insurance-cost-rate insurance cost in percentage of home purchase value
                           (defaults to 0.002)
      :home-sale-cost-rate cost in percentage of home value when home is sold
                           (defaults to 0.06)
      :alternate-investment-return-rate rate of return of alternate investment
                           (defaults to 0.05)
      :income-tax-rate income tax rate to compute tax savings
                       (defaults to 0.33)
      :max-t max number of time units to return data for"
  ([home-price] (buy-vs-rent home-price {}))
  ([home-price options]
   (buy-vs-rent home-price
                {:n 0
                 :mortgage-computations nil
                 :cumulative-opportunity-cost-of-home 0}
                options))
  ([home-price
    {:keys [n
            mortgage-computations
            cumulative-opportunity-cost-of-home]}
    {:keys [downpayment
            closing-cost
            mortgage-ppm
            mortgage-duration-years
            mortgage-interest-rate
            private-mortgage-interest-rate
            rent-ppm
            hoa-ppm
            home-appreciation-rate
            rent-appreciation-rate
            property-tax-rate
            maintenance-cost-rate
            insurance-cost-rate
            home-sale-cost-rate
            income-tax-rate
            alternate-investment-return-rate
            max-t]
     :or {downpayment (* 0.20 home-price)
          closing-cost (* 0.04 home-price)
          mortgage-monthly-payment nil
          mortgage-duration-years 30
          mortgage-interest-rate 0.04
          private-mortgage-interest-rate 0
          rent-ppm 2500
          hoa-ppm 0
          home-appreciation-rate 0
          rent-appreciation-rate 0
          property-tax-rate 0.01
          maintenance-cost-rate 0.01
          insurance-cost-rate 0.002
          home-sale-cost-rate 0.06
          income-tax-rate 0.33
          alternate-investment-return-rate 0.05
          max-t nil}
     :as options}]
   (let [mortgage-computations (or mortgage-computations
                                   (compute-mortgage-balance
                                    home-price
                                    {:downpayment downpayment
                                     :ppm mortgage-ppm
                                     :interest-rate mortgage-interest-rate
                                     :duration-years mortgage-duration-years}))
         cost-property-tax (-> home-price
                               (* property-tax-rate)
                               (/ 12))
         cost-maintenance (-> home-price
                              (* maintenance-cost-rate)
                              (/ 12))
         cost-insurance (-> home-price
                            (* insurance-cost-rate)
                            (/ 12))
         nth-mortgage-computation (nth mortgage-computations n
                                       {:principal 0 :ppm 0 :interest-ppm 0 :principal-ppm 0})
         savings-tax (* (+ (:deductible-interest-ppm nth-mortgage-computation)
                           cost-property-tax)
                        income-tax-rate)
         principal-remaining (:principal nth-mortgage-computation)
         mortgage-ppm (if (pos? principal-remaining)
                        (:ppm nth-mortgage-computation) 0)
         rent-ppm (if (zero? n) rent-ppm (* rent-ppm (inc (/ rent-appreciation-rate 12))))
         interest-ppm (:interest-ppm nth-mortgage-computation)
         principal-ppm (:principal-ppm nth-mortgage-computation)
         home-value (* home-price (Math/pow (inc (/ home-appreciation-rate 12)) n))
         equity-gain (- home-value principal-remaining)
         cost-pmi (if (< equity-gain (* 0.20 home-price))
                    (-> home-price
                        (* private-mortgage-interest-rate)
                        (/ 12))
                    0)
         opportunity-cost-of-downpayment-and-closing-cost
         (* (+ downpayment closing-cost)
            (Math/pow (inc (/ alternate-investment-return-rate 12)) n))
         opportunity-cost-of-home-in-month-n (- (+ mortgage-ppm
                                                   cost-property-tax
                                                   cost-maintenance
                                                   cost-insurance
                                                   cost-pmi
                                                   hoa-ppm)
                                                rent-ppm
                                                savings-tax)
         cumulative-opportunity-cost-of-home (+ (* cumulative-opportunity-cost-of-home
                                                   (inc (/ alternate-investment-return-rate 12)))
                                                opportunity-cost-of-home-in-month-n)
         opportunity-cost (+ opportunity-cost-of-downpayment-and-closing-cost
                             cumulative-opportunity-cost-of-home)
         sale-value (- (* (- 1 home-sale-cost-rate) home-value)
                       principal-remaining)
         continue? (cond
                     (nil? max-t) (pos? principal-remaining)
                     :else (< n max-t))]
     (when continue?
       (lazy-seq (cons (apply array-map [:t n
                                         :y (-> n
                                                (/ 12.0)
                                                Math/floor
                                                Math/round)
                                         :rent-ppm rent-ppm
                                         :savings-tax savings-tax
                                         :mortgage-ppm mortgage-ppm
                                         :interest-ppm interest-ppm
                                         :pmi-ppm cost-pmi
                                         :principal-ppm principal-ppm
                                         :principal principal-remaining
                                         :equity-gain equity-gain
                                         :home-value home-value
                                         :opportunity-cost opportunity-cost
                                         :sale-value sale-value
                                         :profit-from-sale (- sale-value opportunity-cost)])
                       (buy-vs-rent
                        home-price
                        {:n (inc n)
                         :mortgage-computations mortgage-computations
                         :cumulative-opportunity-cost-of-home cumulative-opportunity-cost-of-home}
                        (assoc options :rent-ppm rent-ppm))))))))
