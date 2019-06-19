(ns medals.widget
  (:require
   [reagent.core :as r]
   [ajax.core :as ajax]))


;; Config
;;;;;;;;;;;;;;;;;;;;;;;

; (def config {:data-uri "https://s3-us-west-2.amazonaws.com/reuters.medals-widget/medals.json"
;              :flags-uri "https://s3-us-west-2.amazonaws.com/reuters.medals-widget/flags.png"})

(def config {:data-uri "data/medals.json"
             :flags-uri "images/flags.png"})


;; State
;;;;;;;;;;;;;;;;;;;;;;;
(defn init-state []
  (r/atom {:medals nil
           :sort-key :gold
           :widget-state :ready}))

(defn sort-key [s]
  s)

(defn sort-medals [medals sort-key]
  (println "Sort Medals" medals)
  medals)

(defn process-medals! [state sort-key]
  (println "Process Medals" @state)
  (let [sorted-medals (sort-medals (:raw-data @state) sort-key)]
    (swap! state assoc :medals (take 10 (map-indexed (fn [idx medal]
                                                       (assoc medal
                                                              :index (inc idx)
                                                              :flag "flag"
                                                              :total (+ (:gold medal)
                                                                        (:silver medal)
                                                                        (:bronze medal))))
                                                     sorted-medals)))))

(defn load-data! [state]
  (swap! state assoc :loading? true :error? false)
  (ajax/GET
    (:data-uri config)
    {:handler  (fn [medals]
                 (.log js/console "Loaded Medals" medals)
                 (swap! state assoc :raw-data medals)
                 (process-medals! state :gold)
                 (swap! state assoc :loading? false))
     :error-handler (fn [{:keys [status status-text]}]
                      (.log js/console (str "Error: " status " " status-text))
                      (swap! state assoc :error? true :loading? false))
     :response-format :json
     :keywords? true}))


;; Views
;;;;;;;;;;;;;;;;;;;;;;;
(defn loading-message []
  [:div "LOADING"])

(defn error-message []
  [:div "There was an error loading data from the server. Please try again later."])

(defn medal-row [{:keys [index flag code gold silver bronze total] :as medal}]
  (println "Medal Row")
  [:tr
   [:td index]
   [:td flag]
   [:td code]
   [:td gold]
   [:td silver]
   [:td bronze]
   [:td total]])

(defn medal-table [state sort-key]
  (println "Medal Table")
  (if (:loading? @state)
    [loading-message]
    [:table
     [:thead
      [:tr [:th {:col-span 3}]
       [:th "Gold"]
       [:th "Silver"]
       [:th "Bronze"]
       [:th "TOTAL"]]]
     [:tbody
      (for [medal (:medals @state)]
        (do (println medal)
            ^{:key (:index medal)} [medal-row medal]))]]))



(defn medals
  ([]
   (medals "gold"))
  ([sort]
   (let [state (init-state)
         _ (load-data! state)
         sort-key (sort-key sort)]
     (fn render-fn [sort]
       [:div.medals-widget
        [:div (str @state)]
        [:h3 "Medal Count"]
        [medal-table state sort-key]]))))
