define (require) ->

  ng = require 'angular'
  L = require 'lodash'
  ga = require 'analytics'
  require 'services'
  ChooseDialogueCtrl = require './choose-dialogue'

  # Run-time requirements
  injectables = L.pairs
    scope: '$scope'
    route: '$route'
    http: '$http'
    location: '$location',
    params: '$routeParams'
    to: '$timeout'
    console: '$log'
    Q: '$q'
    $modal: '$modal'
    connectTo: 'connectTo'
    meetRequest: 'meetRequest'
    Histories: 'Histories'
    Mines: 'Mines'
    silentLocation: '$ngSilentLocation'
    serviceStamp: 'serviceStamp'
    notify: 'notify'




  # Test to see if two strings overlap.
  overlaps = (x, y) -> x && y && (x.indexOf(y) >= 0 || y.indexOf(x) >= 0)

  # Get the configured service at the given URL.
  atURL = (url) -> (mines) -> L.find mines, (m) -> overlaps m.root, url

  # Turn a configuration object into a tool.
  # Currently just adds a handles() method.
  toTool = (conf) ->
    handles = conf.handles
    providers = conf.provides

    if Array.isArray handles
      conf.handles = (cat) -> handles.indexOf(cat) >= 0
    else
      conf.handles = (cat) -> handles is cat

    if Array.isArray providers
      conf.provides = (x) -> x in providers
    else
      conf.provides = (x) -> x is providers

    conf

  # Decorator to create injecting constructor.
  inject = (fn) -> (injected...) ->
    for [name, _], idx in injectables
      @[name] = injected[idx]
    fn.call @

  Controllers = ng.module 'steps.history.controllers', ['steps.services']

  Controllers.controller 'HistoryCtrl', class HistoryController

    currentCardinal: 0

    @$inject: (snd for [fst, snd] in injectables)

    constructor: inject ->
      @init()
      @startWatching()
      console.log @

    elide: true

    startWatching: ->
      scope = @scope

      scope.$watchCollection 'items', ->
      #
      #   exporters = []
      #   # debugger
      #   for tool in scope.nextTools when tool.handles 'items'
      #     for key, data of scope.items when data.ids.length
      #       exporters.push {key, data, tool}
      #   otherSteps = (s for s in scope.nextSteps when not s.tool.handles 'items')
      #   scope.nextSteps = otherSteps.concat(exporters)
      #   console.log "^^^^^^ NEXT STEPS (items)", scope.nextSteps


      scope.$watch 'messages', (msgs) ->

        # handlers = L.values msgs
        # otherSteps = (s for s in scope.nextSteps when s.kind isnt 'msg')
        # scope.nextSteps = otherSteps.concat(handlers)
        # console.log "NEXT STEPS (msg)", scope.nextSteps

      scope.$watch 'categories', (cats) ->
        # debugger

      scope.$watch 'ccat', (val) -> console.log "CCAT is now", val



      scope.$watch 'list', (val) ->
        console.log "LIST IS", val
        listHandlers = []
        for tool in scope.nextTools when tool.handles 'list'
          for category in scope.categories
            if tool.ident in category.tools
              listHandlers.push {tool, category: category, data: scope.list}
        # otherSteps = (s for s in scope.nextSteps when not s.tool.handles 'list')
        # scope.nextSteps = otherSteps.concat(listHandlers)
        scope.nextSteps2 = listHandlers
        console.log "NEXT STEPS IS NOW", scope.nextSteps2

        # categories = []
        # scope.nextSteps2 = []
        # for tool in scope.nextTools
        #   if tool.category? and tool.category not in categories
        #     categories.push tool.category
        #     scope.nextSteps2.push tool
        # categories.push "Other"
        # console.log "nextsteps2", scope.nextSteps2
        # scope.categories = categories

    init: ->
      {Histories, Mines, params, http} = @

      # See below for data fetching to fill these.
      @scope.nextTools ?= []
      @scope.providers ?= []
      @scope.messages ?= {}
      @scope.nextSteps ?= []
      @scope.items ?= {}
      @scope.categories ?= []
      @scope.opennextsteps = false

      @scope.collapsed = true # Hide details in reduced real-estate view.
      @scope.state = {expanded: false, nextStepsCollapsed: true}
      @currentCardinal = parseInt params.idx, 10
      @scope.history = Histories.get id: params.id
      @scope.steps = Histories.getSteps id: params.id
      @scope.step = Histories.getStep id: params.id, idx: @currentCardinal - 1
      @mines = Mines.all()

      @scope.showtools = (val) =>

        # console.log "CALLING SHOW TOOLS"

        @scope.ccat = val

        # @scope.item1 = val

        # console.log "CATEGORIES", @scope.categories
        # @scope.nextSteps2 = (tool for tool in @scope.nextTools when tool.category is val)
        # console.log "SHOWING TOOLS", @scope.nextSteps
        # console.log "SCOPE.STEPS", @scope.steps;
        # console.log "SCOPE.HISTORIES", @scope.steps;
        #
        #


        # @scope.nextSteps3 = (s for s in @scope.nextSteps when s.tool.ident in val.tools and !s.kind?)
        # console.log "CAN SHOW THESE......", @scope.nextSteps2




        # else
        #   for item in @scope.nextSteps
        #     console.log "val", item.tool.category
        #   @scope.nextSteps2 = (s for s in @scope.nextSteps when not s.tool.category?)

        # console.log "can show", tool
        # console.log "next steps is now", @scope.nextSteps3
        # console.log "scope cateogyr is", @scope.ccat

      # @scope.talktools = (cat) =>
      #   @scope.cattools = cat.tools
      #   @scope.ccat = cat
      #   console.log "scope cat is", @scope.ccat

      @scope.clearcc = () =>
        @scope.ccat = null

      @scope.showmenu = =>
        @scope.showsubmenu = true

      @scope.hidemenu = =>
        @scope.showsubmenu = false

      @scope.expandhistory = =>
        @scope.openhistory = true

      @scope.shrinkhistory = =>
        @scope.openhistory = false

      @scope.expandnextsteps = =>
        @scope.opennextsteps = true

      @scope.shrinknextsteps = =>
        @scope.opennextsteps = false


      toolNotFound = (e) => @to => @scope.error = e

      unless @scope.tool? # tool never changes! to do so breaks the history API.
        @scope.step.$promise.then ({tool}) =>
          http.get('/tools/' + tool)
                .then (({data}) => @scope.tool = data), toolNotFound
          http.get('/tools', params: {capabilities: 'next'})
              .then ({data}) => @scope.nextTools = data.map toTool
      http.get('/tools', params: {capabilities: 'provider'})
          .then ({data}) -> data.map toTool
          .then (providers) => @scope.providers = providers
      http.get('/tool-categories')
          .then ({data}) => console.log "categories", data; @scope.categories = data;

    saveHistory: ->
      @scope.editing = false
      @scope.history.$save()

    updateHistory: ->
      {Histories, params} = @
      @scope.editing = false
      @scope.history = Histories.get id: params.id

    setItems: -> (key, type, ids) => @set ['items', key], {type, ids}


    # Set scope values using an array of keys.
    # eg: this.set ['foo', 'bar'], 2 == this.scope.foo.bar = 2
    #       (but in a timeout)
    set: ([prekeys..., key], value) -> @to =>
      o = @scope
      for k, i in prekeys
        o = o[k]
      o[key] = value

    hasSomething: (what, data, key) ->
      # debugger
      {scope, console, to, Q, mines} = @
      if what is 'list'
        return to -> scope.list = data

      triggerUpdate = -> to -> scope.messages = L.assign {}, scope.messages

      handlers = (tool for tool in scope.nextTools when tool.handles(what))

      for tool in handlers then do (tool) =>
        idx = tool.ident + what + key
        if data # set or update
          if scope.messages[idx]?
            @set ['messages', idx, 'data'], data
          else
            @connectTo(data['service:base'] ? data.service.root).then (service) =>
              @set ['messages', idx], {tool, data, service, kind: 'msg'}
              triggerUpdate()
        else # delete
          delete scope.messages[idx]

      # trigger the update.
      triggerUpdate() if handlers.length

    letUserChoose: (tools) ->
      dialogue = @$modal.open
        templateUrl: '/partials/choose-tool-dialogue.html'
        controller: ChooseDialogueCtrl
        resolve: {items: tools}

      return dialogue.result

    meetingRequest: (next, data) ->
      if next.length is 1
        @meetRequest(next[0], @scope.step, data)
      else
        @letUserChoose(next).then (provider) => @meetRequest provider, @scope.step, data

    wantsSomething: (what, data) ->
      {notify, console, meetRequest} = @
      console.log "Something is wanted", what, data
      next = @scope.providers.filter (t) -> t.provides what
      console.log "Suitable providers found", next, @scope.providers

      return unless next.length

      @meetingRequest(next, data).then @nextStep, (err) ->
        console.error err
        notify err.message

    storeHistory: (step) -> @nextStep step, true

    stampStep: (step) ->
      {Q, serviceStamp} = @
      if not step.data.service? # Nothing to stamp.
        Q.when step
      else
        serviceStamp(step.data.service).then (stamp) -> L.assign {stamp}, step

    nextStep: (step, silently = false) =>
      {Histories, scope, currentCardinal, console, location, silentLocation} = @
      console.debug "Next step:", step
      console.log "storing - silently? #{ silently }"
      nextCardinal = currentCardinal + 1
      appended = null

      goTo = if silently
        (url) =>
          @params.idx = nextCardinal
          silentLocation.silent url
          @currentCardinal = nextCardinal
          @init()
      else
        (url) -> location.url url

      history = scope.history

      @stampStep(step).then (stamped) ->
        console.log 'STAMPED', stamped
        if history.steps.length isnt currentCardinal
          title = "Fork of #{ history.title }"
          console.debug "Forking at #{ currentCardinal } as #{ title }"
          fork = Histories.fork {id: history.id, at: currentCardinal, title}, ->
            console.debug "Forked history"
            appended = Histories.append {id: fork.id}, stamped, ->
              console.debug "Created step #{ appended.id }"
              goTo "/history/#{ fork.id }/#{ nextCardinal }"
        else
          appended = Histories.append {id: history.id}, stamped, ->
            console.debug "Created step #{ appended.id }"
            goTo "/history/#{ history.id }/#{ nextCardinal }"
            ga 'send', 'event', 'history', 'append', step.tool

  Controllers.controller 'HistoryStepCtrl', Array '$scope', '$log', '$modal', (scope, log, modalFactory) ->

    scope.$watch 'appView.elide', ->
      scope.elide = scope.appView.elide && scope.$middle && (scope.steps.length - scope.$index) > 3

    InputEditCtrl = Array '$scope', '$modalInstance', 'history', 'step', (scope, modal, history, step, index) ->

      scope.data = L.clone step.data, true
      delete scope.data.service # Not editable

      scope.ok = ->
        modal.close history.id, index, scope.data

      scope.cancel = ->
        modal.dismiss 'cancel'
        scope.data = L.clone step.data, true


    scope.openEditDialogue = ->
      dialogue = modalFactory.open
        templateUrl: '/partials/edit-step-data.html'
        controller: InputEditCtrl
        size: 'lg'
        resolve:
          index: -> scope.$index
          history: -> scope.history
          step: -> scope.s