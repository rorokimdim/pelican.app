(ns ^:figwheel-hooks pelican.app
  (:require [reagent.core :as reagent]

            [pelican.db :refer [update-window-dims!
                                update-home-price!]]
            [pelican.components.root :refer [root-comp]]))

(enable-console-print!)

(defn on-window-resize
  "Updates window dimensions on browser resize."
  [event]
  (update-window-dims!))

(reagent/render-component [root-comp]
                          (. js/document (getElementById "app"))
                          (.addEventListener js/window "resize" on-window-resize))

(defn ^:after-load on-reload []
  (println "Reloaded!"))
