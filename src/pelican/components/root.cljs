(ns pelican.components.root
  (:require [pelican.db :refer [get-config
                                VALIDATION
                                get-validation-error]]
            [pelican.analysis :as analysis]
            [pelican.components.config :refer [config-comp]]
            [pelican.components.chart :refer [chart-comp]]
            [pelican.components.summary :refer [summary-comp]]
            [pelican.components.computations :refer [computations-comp]]))

(defn root-comp
  "Builds root component."
  []
  @VALIDATION ;; Force data re-validation if this flag changes
  (let [config (get-config)]
    [:div
     [config-comp]
     (if-let [e (get-validation-error)]
       [:p.error "Bad configuration: " e]
       (let [computations (analysis/buy-vs-rent (:home-price config) (assoc config :max-t 360))]
         (chart-comp computations)
         [:div
          [summary-comp computations]
          [computations-comp computations]]))]))
