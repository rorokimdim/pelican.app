(ns pelican.utils
  (:require [cljs.pprint :as p]))

(defn format-number-with-commas
  "Formats number N with commas."
  [n]
  (p/cl-format nil "~:d" n))

(defn format-number-as-pct
  "Formats number representing a percentage."
  [n]
  (p/cl-format nil "~,2f%" (* 100.0 n)))

(defn value-by-id
  "Gets value of a dom element by id."
  [id default]
  (let [e (.getElementById js/document id)]
    (if e (.-value e) default)))

(defn float-value-by-id
  "Gets float value of a dom element by id."
  [id]
  (js/parseFloat (value-by-id id 0)))

(defn abs
  "Returns absolute value of a number."
  [n]
  (if (neg? n) (* -1 n) n))
