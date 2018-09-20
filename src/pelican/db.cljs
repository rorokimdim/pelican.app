(ns pelican.db
  (:require [reagent.core :as r]
            [alandipert.storage-atom :as ls]

            [pelican.utils :as u]))

(def MAX-ALLOWED-VALUE 1000000000000000)

(def APP-STATE
  (ls/local-storage
   (r/atom
    {:config {:home-price 1000000
              :downpayment 200000
              :closing-cost (* 0.04 1000000)

              :mortgage-duration-years 30
              :mortgage-interest-rate 0.04
              :private-mortgage-interest-rate 0.01
              :mortgage-ppm 3819
              :hoa-ppm 0

              :home-appreciation-rate 0.04
              :alternate-investment-return-rate 0.07

              :income-tax-rate 0.33
              :property-tax-rate 0.01
              :maintenance-cost-rate 0.01
              :insurance-cost-rate 0.002

              :home-sale-cost-rate 0.06

              :rent-ppm 3500
              :rent-appreciation-rate 0.01}})
   :APP-STATE))

(defonce VALIDATION (r/atom 0)) ;; Atom to notify that we need to re-validate configuration data

(defonce WINDOW-DIMS
  (r/atom
   {:width (.-innerWidth js/window)
    :height (.-innerHeight js/window)}))

(defn reset-validation! [flag]
  (reset! VALIDATION (+ @VALIDATION flag)))

(defn get-config
  "Gets current configuration."
  []
  (:config @APP-STATE))

(defn get-window-width
  "Gets current window width."
  []
  (:width @WINDOW-DIMS))

(defn get-window-height
  "Gets current window height."
  []
  (:height @WINDOW-DIMS))

(defn update-window-dims!
  "Updates window dimensions to current window dimensions."
  []
  (reset! WINDOW-DIMS
          {:width (.-innerWidth js/window)
           :height (.-innerHeight js/window)}))

(defn update-config!
  "Updates configuration for KEY with VALUE."
  [key value]
  (swap! APP-STATE assoc-in [:config key] value))

(defn reset-config!
  "Resets configuration to default values."
  []
  (ls/clear-local-storage!))

(defn update-home-price!
  "Updates home price."
  [home-price]
  (swap! APP-STATE assoc-in [:config :home-price] home-price))

(defn get-validation-error
  "Gets the first configuration error found."
  []
  (let [config (get-config)
        invalid-value-key (some #(when (js/isNaN (u/float-value-by-id (name %))) %) (keys config))
        too-big-value-key (some #(when (< MAX-ALLOWED-VALUE
                                          (u/abs (u/float-value-by-id (name %)))) %) (keys config))
        home-price (:home-price config)
        downpayment (:downpayment config)]
    (cond
      invalid-value-key (str invalid-value-key " must be a number.")
      too-big-value-key (str too-big-value-key " is extremely large.")
      (> downpayment home-price) (str "Downpayment cannot be larger than home-price")
      :else nil)))
