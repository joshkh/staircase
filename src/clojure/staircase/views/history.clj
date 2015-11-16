(ns staircase.views.history
  (:require staircase.views.forms
            staircase.views.facets
            [hiccup.def :refer (defelem)]
            [staircase.views.buttons :as btn])
  (:use [hiccup.core :only (html)]
        [hiccup.element :only (mail-to)]))

(defelem tool-not-found [{contact :contact-email}]
  [:div.alert.alert-danger
   [:h3 "Error"]
   [:p
    " The required tool for this step could not be found. Check with the site
    administrator to make sure that the "
    [:code "{{step.tool}}"]
    " tool is installed"]
   (update-in (mail-to contact
                       [:span
                        [:i.fa.fa-envelope-o]
                        " Send a grumpy email"])
              [1 :class] (constantly "btn btn-default"))])

(def current-history
  [:div.current-history.panel.panel-default
   (apply vector
          :div.panel-heading
          (btn/collapse-button)
          (btn/edit-button)
          (btn/cancel-button {:ng-click "appView()"})
          (btn/save-button {:ng-click "appView()"})
          (staircase.views.forms/label-input-pair "history.title"))
   (apply vector
          :div.panel-body
          [:p "test"]
          [:p [:small [:em "{{history.created_at | roughDate }}"]]]
          (staircase.views.forms/label-input-pair "history.description"))
   [:div.flex-row.stretched
    {:ng-if "steps.length > 4"}
    [:a.toggle-elisions {:ng-click "appView.elide = !appView.elide"}
     [:small "show all steps"]]]
    [:div.list-group.history-steps
     [:a.list-group-item {:ng-repeat "s in steps"
                          :ng-controller "HistoryStepCtrl as stepCtrl"
                          :ng-class "{active: step.id == s.id}"
                          :href "/history/{{history.id}}/{{$index + 1}}"
                          :folded "elide"}
      [:i.pull-right.fa.fa-edit
       {:ng-click "openEditDialogue()"
        :ng-show "step.id == s.id"}]
      "{{s.title}}"]]
    ])

; (defn left-column [config]
;   [:div.sidebar-left
;    {:ng-class "{expand: showhistory}"
;     :ng-mouseleave "shrinkhistory()"
;     :ng-mouseenter "expandhistory()"}
;    current-history
;    (staircase.views.facets/snippet)])

; (defn left-column [config]
;  [:div.history-blind
;   {:ng-class "{open: openhistory}"
;   :ng-mouseleave "shrinkhistory()"
;   :ng-mouseenter "expandhistory()"}
;   [:div.history-container
;    [:div.previous-steps
;     [:div.previous-step
;      [:div.summary
;       [:i.fa.fa-cubes.fa-2x]
;       [:div "Show List Tool"]]
;      [:div.details "Details"]]]]])
      ; current-history
      ; (staircase.views.facets/snippet)])



(defn left-column [config]
 [:div.blind.left
  {:ng-class "{open: openhistory}"
  :ng-mouseleave "shrinkhistory()"
  :ng-mouseenter "expandhistory()"
  :scrollable ""}
  [:div.contents-container
   [:div.steps
    [:a
      [:div.step.hot
       [:div.summary
        [:div.step-header "Previous Steps"]]]]
    [:a {:ng-href "/history/{{history.id}}/{{steps.length - $index}}"
        :ng-repeat "s in steps | reverse"
         :ng-controller "HistoryStepCtrl as stepCtrl"}
    [:div.step
      ; {:ng-class "{hot: step.id == s.id}"}
     [:div.summary
      [:span.badge.numbering "{{steps.indexOf(s) + 1}}"]
      [:i.fa.fa-cubes.fa-2x]
      [:div "{{s.title}}"]]
     [:div.details {:ng-class "{transparent: !openhistory}"} "{{s.description}}"]]]]]])


 (defn right-column2 [config]
  [:div.blind.right
   {:ng-class "{open: opennextsteps}"
   :ng-mouseleave "shrinknextsteps(); clearcc()"
   :scrollable ""
   }
   [:div.contents-container
   [:div.step.hot
    [:div.summary [:div.step-header "Using {{list.size}} {{list.type}}(s)"]]]
    [:div.steps.right {:ng-mouseenter "expandnextsteps()"}
    [:div.details.right
        [:next-step
         {:ng-repeat "ns in nextSteps2"
          :previous-step "step"
          :append-step "appView.nextStep(data)"
          :tool "ns.tool"
          :category "ns.category"
          :service "ns.service"
          :ng-show "ns.category.label==ccat.label"
          :data "ns.data"}]]
     [:a {:ng-href "#"
         :ng-repeat "category in categories"
         :ng-mouseenter "showtools(category)"}
          ; :ng-controller "HistoryStepCtrl as stepCtrl"}
     [:div.step.empty {:ng-class "{hot: step.id == s.id}"}
    ;  [:div.details {:ng-class "{transparent: !opennextsteps}"} "{{category.label}}"]
    ;  [:div.details.transparent "{{s.label}}"]
      [:div.summary {:ng-class "{highlighted: category.label == ccat.label}"}
      ;  [:span.badge.numbering "{{category.tools.length}}"]
       [:i.fa-2x {:class "{{category.icon}}"}]
       [:div "{{category.label}}"]]
      ]]]]])

 ; (defn right-column2 [config]
 ;  [:div.blind.right
 ;   {:ng-class "{open: opennextsteps}"
 ;   :ng-mouseleave "shrinknextsteps(); clearcc()"
 ;   :ng-mouseenter "expandnextsteps()"}
 ;   [:div.contents-container
 ;    [:div.next-steps
 ;    [:div.details
 ;     [:div.listitem {:ng-repeat "tool in cattools"} "{{tool}}"]
 ;     ]
 ;     [:div.categories
 ;       [:a {:ng-href "#"
 ;           :ng-repeat "category in categories"
 ;            :ng-controller "HistoryStepCtrl as stepCtrl"
 ;            :ng-mouseenter "talktools(category)"}
 ;            [:div.summary {:ng-class "{highlighted: category.label == ccat.label}"}
 ;                  [:span.badge.numbering "{{category.tools.length}}"]
 ;                  [:i.fa-2x {:class "{{category.icon}}"}]
 ;                  [:div "{{category.label}}"]]]]
 ;
 ;       ]]])



;; THIS WORKS!!!
; (defn right-column2 [config]
;  [:div.blind.right
;   {:ng-class "{open: opennextsteps}"
;   :ng-mouseleave "shrinknextsteps()"
;   :ng-mouseenter "expandnextsteps()"}
;   [:div.contents-container
;    [:div.previous-steps
;     [:a {:ng-href "#"
;         :ng-repeat "category in categories"
;          :ng-controller "HistoryStepCtrl as stepCtrl"
;          :ng-mouseenter "talktools(category)"}
;     [:div.previous-step {:ng-class "{hot: step.id == s.id}"}
;      [:div.summary
;       [:span.badge.numbering "{{category.tools.length}}"]
;       [:i.fa-2x {:class "{{category.icon}}"}]
;       [:div "{{category.label}}"]]
;      [:div.details {:ng-class "{transparent: !opennextsteps}"}
;        [:ul
;          [:li {:ng-repeat "tool in category.tools"} "{{tool}} is some useful tool"]]]]]]]])


      (defn super-menu [config]
        [:div.blind.right
          {:ng-mouseleave "hidemenu()"
            :ng-mouseenter "showmenu()"}
          [:div.header
            [:span.count "172"]
            [:span.type "Genes"]]
          [:div.main-menu
          ;  [:div {:ng-repeat "category in categories"
          ;   :ng-mouseover "showtools(category)"} "{{category}}"]
          ; [:i.fa.fa-bar-chart.fa-2x]
            [:ul
             [:li {:ng-repeat "category in categories"
                   :ng-mouseover "showtools(category)"}
              [:a {:href "#"}
                [:i.fa.fa-bar-chart.fa-2x]
                [:span "{{category}}"]]]]
          ]
          [:div.sub-menu
          {:ng-hide "!showsubmenu"}
          ;  [:div {:ng-repeat "tool in tools"} "{{tool.ident}}"]
           [:div.list-group.steps.expanded
            [:next-step
             {:ng-repeat "ns in nextSteps2"
              :previous-step "step"
              :append-step "appView.nextStep(data)"
              :tool "ns.tool"
              :service "ns.service"
              :data "ns.data"}]]]])



(defn right-column [config]
  [:div.sidebar.slide-right.col-xs-12.col-md-2.col-md-offset-10
   {:ng-class "{onscreen: !state.expanded, offscreen: state.expanded}"}
   [:div.panel.panel-default.next-steps
    [:div.panel-heading
     {:ng-click "state.nextStepsCollapsed = !state.nextStepsCollapsed"}
     [:i.fa.fa-fw {:ng-class "{'fa-caret-right': state.nextStepsCollapsed, 'fa-caret-down': !state.nextStepsCollapsed}"}]
     "Next steps"]
    [:div.list-group.steps.animated
     {:ng-class "{expanded: (nextSteps.length && !state.nextStepsCollapsed)}"}
     [:next-step
      {:ng-repeat "ns in nextSteps"
       :previous-step "step"
       :append-step "appView.nextStep(data)"
       :tool "ns.tool"
       :service "ns.service"
       :data "ns.data"}
      ]]
    [:div.panel-body {:ng-hide "nextSteps.length"}
     [:em "No steps available"]]]])




(defn centre-column [config]
  [:div.central-panel
  ;  {:ng-class "{'col-md-8': !state.expanded,
  ;             'col-md-offset-2': !state.expanded}"}
   (tool-not-found {:ng-show "error.status === 404"} config)
   [:div.current-step
    {:ng-hide "error.status === 404"
     :tool "tool"
     :step "step"
     :state "state"
     :has-items "appView.setItems(key, type, ids)"
     :has "appView.hasSomething(what, data, key)"
     :wants "appView.wantsSomething(what, data)"
     :next-step "appView.nextStep(data)"
     :silently "appView.storeHistory(data)"
     :toggle "state.expanded = !state.expanded"} ]])

(defn snippet [config]
  (html [:div.container-fluid.history-view
         (apply vector :div.row.flex-row
          ;; L R C because of column re-ordering.
          ((juxt left-column centre-column right-column2) config))]))
