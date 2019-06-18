(ns medals.widget
  (:require
   [reagent.core :as r]))


(defn table [data]
  [:table
   [:tr [:th {:colspan 3}]
    [:th "Gold"]
    [:th "Silver"]
    [:th "Bronze"]
    [:th "TOTAL"]]])


(defn medals
  ([]
   (medals "gold"))
  ([sort]
   [:div.medals-widget
    [:h3 "Medal Count"]
    [table [{}]]]))